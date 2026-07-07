#!/usr/bin/env python3
"""
从 Jamendo 随机抓 N 首可下载的 CC 音乐 → 上传 MinIO(bucket: music, key: <歌名>.mp3)
→ 生成 sql/05_jamendo_music.sql(music 表种子, id 201+)。

说明:
- Jamendo 公开 API 需要 client_id(demo id 已停用),这里走两步免鉴权路径:
  1) https://www.jamendo.com/track/{id} 页面的 SEO <title> 提供 "艺人 - 歌名";
  2) https://prod-1.storage.jamendo.com/?trackid={id}&format=mp32 匿名直链下载。
- 曲风 tag 优先从页面 ld+json 的 genre 解析并映射到前端 8 个预设;拿不到则轮转分配,
  保证"按兴趣推荐"有数据可匹配。
- 幂等:重复跑会重新随机选曲;SQL 用固定 id 201.. + INSERT IGNORE,不会重复插行。

依赖: curl、~/minio-tools/mc(alias localminio 已配置)。用法: python3 scripts/fetch-jamendo-music.py
"""
import html
import os
import random
import re
import subprocess
import sys
import tempfile

N = 10
START_ID = 201  # music 表起始 id(避开 ≤124 的既有数据)
MC = os.path.expanduser("~/minio-tools/mc")
BUCKET = "localminio/music"
SQL_OUT = os.path.join(os.path.dirname(__file__), "..", "sql", "05_jamendo_music.sql")
PRESETS = ["Pop", "Rock", "Hip-Hop/Rap", "Electronic Dance Music (EDM)",
           "Classical", "Jazz", "Country", "R&B"]
# jamendo genre 关键词 → 我们的预设 tag
GENRE_MAP = [
    (re.compile(r"hip.?hop|rap", re.I), "Hip-Hop/Rap"),
    (re.compile(r"electro|techno|house|dance|edm|trance|dubstep", re.I), "Electronic Dance Music (EDM)"),
    (re.compile(r"rock|metal|punk|grunge", re.I), "Rock"),
    (re.compile(r"classical|orchestra|piano|symph", re.I), "Classical"),
    (re.compile(r"jazz|swing|blues", re.I), "Jazz"),
    (re.compile(r"country|folk", re.I), "Country"),
    (re.compile(r"r&b|rnb|soul|funk", re.I), "R&B"),
    (re.compile(r"pop", re.I), "Pop"),
]


def curl(url, out=None, timeout=40):
    """curl 包装:out=None 返回文本,否则落盘返回 True/False。"""
    cmd = ["curl", "-sL", "--max-time", str(timeout), "-A", "Mozilla/5.0", url]
    if out:
        r = subprocess.run(cmd + ["-o", out], capture_output=True)
        return r.returncode == 0
    r = subprocess.run(cmd, capture_output=True)
    return r.stdout.decode("utf-8", "ignore") if r.returncode == 0 else ""


def pick_genre(page, idx):
    m = re.search(r'"genre"\s*:\s*"([^"]+)"', page)
    if m:
        for pat, preset in GENRE_MAP:
            if pat.search(m.group(1)):
                return preset
    return PRESETS[idx % len(PRESETS)]


def main():
    rng = random.Random()
    picked, seen_ids, seen_titles = [], set(), set()
    tmpdir = tempfile.mkdtemp(prefix="jamendo_")
    attempts = 0

    while len(picked) < N and attempts < 120:
        attempts += 1
        tid = rng.randint(1000, 2000000)
        if tid in seen_ids:
            continue
        seen_ids.add(tid)

        page = curl(f"https://www.jamendo.com/track/{tid}")
        m = re.search(r"<title>(.*?)\s*\|\s*Jamendo", page, re.S)
        if not m or " - " not in m.group(1):
            continue  # 曲目不存在/已下架(重定向到通用页)
        artist, title = html.unescape(m.group(1)).split(" - ", 1)
        artist, title = artist.strip(), title.strip().replace("/", "-")
        if not artist or not title or title.lower() in seen_titles:
            continue

        ym = re.search(r'"datePublished"\s*:\s*"(\d{4})', page)
        year = int(ym.group(1)) if ym else 2020

        # 下载音频(优先 mp32 高音质,失败退 mp31)
        path = os.path.join(tmpdir, f"{tid}.mp3")
        got = False
        for fmt in ("mp32", "mp31"):
            if curl(f"https://prod-1.storage.jamendo.com/?trackid={tid}&format={fmt}&from=app-devsite", out=path):
                if os.path.getsize(path) > 200_000:
                    with open(path, "rb") as f:
                        head = f.read(3)
                    if head[:3] == b"ID3" or head[:2] == b"\xff\xfb" or head[:2] == b"\xff\xf3":
                        got = True
                        break
        if not got:
            continue

        genre = pick_genre(page, len(picked))
        key = f"{title}.mp3"
        r = subprocess.run([MC, "cp", "--attr", "Content-Type=audio/mpeg", path, f"{BUCKET}/{key}"],
                           capture_output=True)
        if r.returncode != 0:
            print(f"  [skip] mc 上传失败 {key}: {r.stderr.decode()[:120]}", file=sys.stderr)
            continue

        seen_titles.add(title.lower())
        size_mb = os.path.getsize(path) / 1e6
        picked.append({"tid": tid, "title": title, "artist": artist, "year": year, "genre": genre})
        print(f"  [{len(picked)}/{N}] {artist} - {title}  ({genre}, {year}, {size_mb:.1f}MB, jamendo#{tid})")

    if len(picked) < N:
        print(f"只抓到 {len(picked)} 首(尝试 {attempts} 次),SQL 仍会生成", file=sys.stderr)

    esc = lambda s: s.replace("'", "''")
    rows = ",\n".join(
        f"  ({START_ID + i}, '{esc(t['title'])}', '{esc(t['artist'])}', {t['year']}, "
        f"'{esc(t['genre'])}', NULL, 'ONLINE')"
        for i, t in enumerate(picked)
    )
    with open(os.path.abspath(SQL_OUT), "w", encoding="utf-8") as f:
        f.write(
            "-- Jamendo 真实音乐种子(音频在 MinIO bucket `music`, key = `<title>.mp3`)\n"
            "-- 由 scripts/fetch-jamendo-music.py 生成;幂等(固定 id + INSERT IGNORE)。\n"
            f"-- 来源 track: {', '.join(str(t['tid']) for t in picked)}\n\n"
            "USE `test`;\n\n"
            "INSERT IGNORE INTO `music` (`id`, `title`, `artist`, `release_year`, `tags`, `lyrics`, `status`) VALUES\n"
            + rows + ";\n"
        )
    print(f"\nSQL 已生成: sql/05_jamendo_music.sql ({len(picked)} 行, id {START_ID}..{START_ID + len(picked) - 1})")


if __name__ == "__main__":
    main()
