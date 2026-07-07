import { prizeClient } from './client'
import type { PrizeListVO, PrizeCountVO, PrizeFlowListVO, PrizeCmd, BaseVO } from './types'

/** PrizeCenter (8082): one service manages all prizes; code distinguishes kind. */
export const prizeApi = {
  all: () => prizeClient.get<PrizeListVO>('/prize/getallprize').then((r) => r.data),
  search: (code: string) => prizeClient.get<any>('/prize/search', { params: { code } }).then((r) => r.data),
  add: (cmd: PrizeCmd) => prizeClient.post<BaseVO>('/prize/add', cmd).then((r) => r.data),
  update: (id: number, storage: number, name: string, description: string) =>
    prizeClient.put<BaseVO>('/prize/update', null, { params: { id, storage, name, description } }).then((r) => r.data),
  remove: (id: number) => prizeClient.delete<BaseVO>('/prize/delete', { params: { id } }).then((r) => r.data),
  sendReward: (code: string, amount: number, outbizno: string) =>
    prizeClient.post<BaseVO>('/prize/send_reward', { code, amount, outbizno }).then((r) => r.data),

  records: () => prizeClient.get<PrizeFlowListVO>('/prize_reward/get_all').then((r) => r.data),
  count: () => prizeClient.get<PrizeCountVO>('/prize_reward/count').then((r) => r.data),
  recordByOutBiz: (outbizno: string) =>
    prizeClient.get<any>('/prize_reward/out_biz', { params: { outbizno } }).then((r) => r.data),
}
