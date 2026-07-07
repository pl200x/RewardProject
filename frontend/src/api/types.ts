// TypeScript types matching backend VO/Cmd shapes. Optional fields tolerate runtime variance.

export interface BaseVO {
  code: number
  time?: number
  duration?: number
  success: boolean
  errorMessage: string | null
}

/** Login response from Music_management with JWT. */
export interface LoginResp {
  token: string | null
  userId: number | null
  userName: string | null
  /** Current interests as comma-separated text; empty means first-time onboarding. */
  interest?: string | null
  /** Current avatar index 0-9. */
  avatar?: number | null
  baseVO: BaseVO
}

// ---- Music_management ----
/** Lightweight list summary without heavy fields such as lyrics. */
export interface MusicDigest {
  id: number
  title: string
  artist: string
  tags?: string | null
  score?: number
}
export interface Music {
  id: number
  title: string
  artist: string
  releaseYear?: number
  tags?: string
  lyrics?: string | null
  status?: string
}
export interface RecommendMusicVO {
  musicDigestVOList: MusicDigest[]
  baseVO: BaseVO
}
/** Shared detail item for the home columns, matching backend MusicDetailVO. */
export interface MusicDetail {
  id: number
  title: string
  artist: string
  releaseYear?: number
  tags?: string | null
  lyrics?: string | null
  status?: string
  ranking?: number
  score?: number
}
/** /music/id detail response, matching backend MusicPageVO. */
export interface MusicPageVO {
  musicDetailVO: MusicDetail | null
  baseVO: BaseVO
}
/** /music/audio-url: MinIO presigned playback URL. success=false when missing. */
export interface AudioUrlVO {
  url: string | null
  baseVO: BaseVO
}
/** /music/upload returns the new song id on success. */
export interface UploadMusicVO {
  musicId: number | null
  baseVO: BaseVO
}
/** Lightweight user summary for like avatar walls and comments. */
export interface UserDigestVO {
  userId: number
  userName: string | null
  avatar: number
}
export interface MusicLikePageVO {
  musicId: number
  userIds: number[]
  /** Unique users who liked the song, ordered by like time, with name/avatar. */
  users?: UserDigestVO[]
  baseVO: BaseVO
}
export interface UserLikePageVO {
  userId: number
  musicIds?: number[]
  baseVO: BaseVO
}
/** One comment. time is epoch milliseconds; name/avatar are filled by backend. */
export interface CommentVO {
  userId: number
  userName?: string | null
  avatar?: number
  content: string
  time: number
}
export interface MusicCommentPageVO {
  musicId: number
  comments: CommentVO[] | null
  totalComment: number
  baseVO: BaseVO
}

// ---- Multi_reward ----
export interface Configuration {
  id: number
  code: string
  rule: string | null
  description: string
  createTime?: string
  updateTime?: string
}
export interface ConfigurationListVO {
  configurationVoList: Configuration[]
  baseVO: BaseVO
}
/** Today's accumulated analysis; totalDuration drives the progress bar. */
export interface UserAnalysis {
  id: number
  userId: number
  summaryDate: string
  totalDuration: number
  lastPlayTime?: string
}
export interface UserAnalysisPageVO {
  userAnalysisVO: UserAnalysis | null
  baseVO: BaseVO
}
/** User-level reward record from Multi_reward. */
export interface RewardRecord {
  id: number
  userId: number
  bizScene?: string
  prizeCode: string
  prizeDate: string
  prizeStage: number
  prizeAmount: number
  outBizNo: string
}
export interface RewardRecordPageVO {
  prizeRecordVOList: RewardRecord[]
  baseVO: BaseVO
}

// ---- PrizeCenter ----
export interface Prize {
  id: number
  code: string
  name: string
  description: string
  storage: number
  createTime?: string | null
  updateTime?: string | null
}
export interface PrizeListVO {
  prizeVOList: Prize[]
  baseVO: BaseVO
}
export interface PrizeCountVO {
  count: number
  baseVO: BaseVO
}
/** PrizeCenter reward flow record for the admin records page. */
export interface PrizeFlowRecord {
  id: number
  code: string
  amount: number
  outbizno: string
  createTime?: string | null
}
export interface PrizeFlowListVO {
  prizeRecordVOList: PrizeFlowRecord[]
  baseVO: BaseVO
}

// ---- Cmds ----
export interface RegisterCmd {
  name: string
  password: string
  age: number
  gender: number
  email: string
  job: string
  interest: string
}
export interface MusicCmd {
  title: string
  artist: string
  releaseYear: number
  tags: string
  lyrics: string
}
export interface PrizeCmd {
  code: string
  name: string
  description: string
  storage: number
}
export interface ConfigurationCmd {
  code: string
  rule: string
  description: string
}
export interface PlayMusicCmd {
  userId: number
  musicId: number
  syncTime: string
  duration: number
  scene: string
  code: string
}
