import { musicClient } from './client'
import type {
  LoginResp,
  RegisterCmd,
  MusicCmd,
  PlayMusicCmd,
  RecommendMusicVO,
  MusicLikePageVO,
  UserLikePageVO,
  MusicCommentPageVO,
  MusicPageVO,
  AudioUrlVO,
  UploadMusicVO,
  BaseVO,
} from './types'

/** Music_management (8081) through musicClient with automatic JWT. */
export const musicApi = {
  // Users
  login: (userName: string, password: string) =>
    musicClient.get<LoginResp>('/user/login', { params: { userName, password } }).then((r) => r.data),
  register: (cmd: RegisterCmd) => musicClient.post<BaseVO>('/user/register', cmd).then((r) => r.data),
  logout: (userId: number) => musicClient.delete<BaseVO>('/user/logout', { params: { userId } }).then((r) => r.data),
  changeTag: (userId: number, interest: string) =>
    musicClient.post<BaseVO>('/user/change-tag', null, { params: { userId, interest } }).then((r) => r.data),
  changeAvatar: (userId: number, avatar: number) =>
    musicClient.post<BaseVO>('/user/change-avatar', null, { params: { userId, avatar } }).then((r) => r.data),
  usersByInterest: (interest: string) =>
    musicClient.get<any>('/interest', { params: { interest } }).then((r) => r.data),

  // Music
  recommend: () => musicClient.get<RecommendMusicVO>('/music/list').then((r) => r.data),
  /** Full detail with lyrics/ranking, fetched on demand by the detail page. */
  musicById: (id: number) => musicClient.get<MusicPageVO>('/music/id', { params: { id } }).then((r) => r.data),

  // Home columns as digest lists without lyrics: top, recent, and interest-based.
  top: (N = 10) => musicClient.get<RecommendMusicVO>('/rank/top', { params: { N } }).then((r) => r.data),
  recent: (n = 10) => musicClient.get<RecommendMusicVO>('/music/recent', { params: { n } }).then((r) => r.data),
  mayLike: (userId?: number, N = 10) =>
    musicClient.get<RecommendMusicVO>('/rank/recommend', { params: { userId, N } }).then((r) => r.data),

  // Public search returning digest lists; misses include baseVO.errorMessage.
  search: (name: string) =>
    musicClient.get<RecommendMusicVO>('/music/search', { params: { name } }).then((r) => r.data),

  // Public MinIO presigned audio URL. Missing audio returns success=false.
  audioUrl: (id: number, silent = true) =>
    musicClient.get<AudioUrlVO>('/music/audio-url', { params: { id }, silent } as any).then((r) => r.data),

  // Upload music with auth, multipart metadata + mp3 file, and progress callback.
  upload: (form: FormData, onProgress?: (pct: number) => void) =>
    musicClient
      .post<UploadMusicVO>('/music/upload', form, {
        timeout: 120000,
        onUploadProgress: (e) => {
          if (onProgress && e.total) onProgress(Math.round((e.loaded / e.total) * 100))
        },
      })
      .then((r) => r.data),
  addMusic: (cmd: MusicCmd) => musicClient.post<BaseVO>('/music/add', cmd).then((r) => r.data),
  deleteMusic: (musicId: number) =>
    musicClient.delete<BaseVO>('/music/id', { params: { musicId } }).then((r) => r.data),

  // Playback report that triggers the reward chain.
  play: (cmd: PlayMusicCmd) => musicClient.post<BaseVO>('/music/play', cmd).then((r) => r.data),

  // Likes
  like: (userId: number, musicId: number) =>
    musicClient.post<BaseVO>('/like/add', null, { params: { userId, musicId } }).then((r) => r.data),
  unlike: (userId: number, musicId: number) =>
    musicClient.delete<BaseVO>('/like/delete', { params: { userId, musicId } }).then((r) => r.data),
  likesByUser: (userId: number, silent = false) =>
    musicClient.get<UserLikePageVO>('/like/getuserall', { params: { userId }, silent } as any).then((r) => r.data),
  likesByMusic: (musicId: number) =>
    musicClient.get<MusicLikePageVO>('/like/getmusicall', { params: { musicId } }).then((r) => r.data),

  // Comments. Posting requires login; reading is public.
  commentAdd: (userId: number, musicId: number, content: string) =>
    musicClient.post<BaseVO>('/comment/add', null, { params: { userId, musicId, content } }).then((r) => r.data),
  commentsByMusic: (musicId: number) =>
    musicClient.get<MusicCommentPageVO>('/comment/getmusicall', { params: { musicId } }).then((r) => r.data),

  // Ranking / Kafka
  rankTop: (N: number) => musicClient.get<any>('/rank/top', { params: { N } }).then((r) => r.data),
  kafkaSend: (content: string) =>
    musicClient.get<any>('/producer/send', { params: { content } }).then((r) => r.data),
}
