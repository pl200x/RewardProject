/** Default avatar URL from the MinIO avatars bucket by index 0-9. */
export const AVATAR_COUNT = 10

export function avatarUrl(n: number | null | undefined): string {
  const i = Number.isInteger(n) && (n as number) >= 0 && (n as number) < AVATAR_COUNT ? (n as number) : 0
  return `/minio/avatars/${i}.svg`
}
