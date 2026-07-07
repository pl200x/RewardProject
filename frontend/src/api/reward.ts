import { rewardClient } from './client'
import type {
  ConfigurationListVO,
  ConfigurationCmd,
  BaseVO,
  UserAnalysisPageVO,
  RewardRecordPageVO,
} from './types'

/** Multi_reward (8080) */
export const rewardApi = {
  // Reward rules (JEXL)
  allConfig: () => rewardClient.get<ConfigurationListVO>('/config/searchall').then((r) => r.data),
  searchConfig: (code: string) => rewardClient.get<any>('/config/search', { params: { code } }).then((r) => r.data),
  addConfig: (cmd: ConfigurationCmd) => rewardClient.post<BaseVO>('/config/add', cmd).then((r) => r.data),
  updateConfig: (code: string, rule: string, description: string) =>
    rewardClient.put<BaseVO>('/config/update', null, { params: { code, rule, description } }).then((r) => r.data),
  deleteConfig: (code: string) => rewardClient.delete<BaseVO>('/config/delete', { params: { code } }).then((r) => r.data),

  // Analysis / records. Silent reads tolerate missing records before playback.
  analysis: (userId: number, summaryDate: string, silent = false) =>
    rewardClient
      .get<UserAnalysisPageVO>('/analysis/id', { params: { userId, summaryDate }, silent } as any)
      .then((r) => r.data),
  prizeByOutBiz: (outBizNo: string) =>
    rewardClient.get<any>('/prizerecord/by-outbizno', { params: { outBizNo } }).then((r) => r.data),
  prizeByUserDate: (userId: number, prizeDate: string, silent = false) =>
    rewardClient
      .get<RewardRecordPageVO>('/prizerecord/by-user-and-date', { params: { userId, prizeDate }, silent } as any)
      .then((r) => r.data),
}
