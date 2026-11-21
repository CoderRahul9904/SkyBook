import { apiClient, unwrapData } from './client'
import type {
  AiChatResponse,
  AiConversationDetailResponse,
  AiConversationSummaryResponse,
} from '../../types/domain'

export interface AiChatPayload {
  message: string
  conversationId?: string
}

export function sendChat(payload: AiChatPayload): Promise<AiChatResponse> {
  return unwrapData(apiClient.post('/api/v1/ai/chat', payload))
}

export function listConversations(): Promise<AiConversationSummaryResponse[]> {
  return unwrapData(apiClient.get('/api/v1/ai/conversations'))
}

export function getConversation(conversationId: string): Promise<AiConversationDetailResponse> {
  return unwrapData(apiClient.get(`/api/v1/ai/conversations/${conversationId}`))
}
