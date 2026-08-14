import { chatbotClient } from './client'

export interface ChatbotResponse {
  code: number
  time: number
  success: boolean
  errorMessage: string | null
  data: string | null
}

export const chatbotApi = {
  async chat(sessionId: string, userInput: string): Promise<ChatbotResponse> {
    const form = new URLSearchParams({
      sessionId,
      userInput,
    })
    const response = await chatbotClient.post<ChatbotResponse>('/chat', form, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })
    return response.data
  },
}
