import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { EmptyBlock, ErrorBlock, LoadingBlock } from '../components/common/Feedback'
import { getConversation, listConversations, sendChat } from '../services/api/aiApi'
import { getApiErrorMessage } from '../services/api/client'
import type {
  AiConversationDetailResponse,
  AiConversationMessageResponse,
  AiConversationSummaryResponse,
} from '../types/domain'
import { formatDateTime } from '../utils/format'

export function AeroMatePage() {
  const [conversations, setConversations] = useState<AiConversationSummaryResponse[]>([])
  const [activeConversationId, setActiveConversationId] = useState<string>('')
  const [messages, setMessages] = useState<AiConversationMessageResponse[]>([])
  const [messageInput, setMessageInput] = useState('')
  const [error, setError] = useState('')
  const [lastFailedMessage, setLastFailedMessage] = useState('')
  const [loadingConversations, setLoadingConversations] = useState(true)
  const [sending, setSending] = useState(false)

  const loadConversations = useCallback(async () => {
    setLoadingConversations(true)
    setError('')
    try {
      const data = await listConversations()
      setConversations(data)
      if (data.length > 0) {
        await selectConversation(data[0].conversationId)
      }
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setLoadingConversations(false)
    }
  }, [])

  useEffect(() => {
    void loadConversations()
  }, [loadConversations])

  async function selectConversation(conversationId: string) {
    setError('')
    try {
      const detail: AiConversationDetailResponse = await getConversation(conversationId)
      setActiveConversationId(detail.conversationId)
      setMessages(detail.messages)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  async function sendMessage(message: string) {
    if (!message) {
      return
    }

    setSending(true)
    setError('')

    try {
      const chat = await sendChat({
        message,
        conversationId: activeConversationId || undefined,
      })

      setMessageInput('')
      setLastFailedMessage('')
      setActiveConversationId(chat.conversationId)

      await Promise.all([loadConversations(), selectConversation(chat.conversationId)])
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
      setLastFailedMessage(message)
    } finally {
      setSending(false)
    }
  }

  async function onSendMessage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await sendMessage(messageInput.trim())
  }

  async function onRetry() {
    if (!lastFailedMessage) {
      return
    }

    await sendMessage(lastFailedMessage)
  }

  return (
    <section className="grid gap-4 xl:grid-cols-[300px_1fr]">
      <aside className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <div className="mb-3 flex items-center justify-between">
          <h1 className="text-lg font-black text-slate-900">AeroMate</h1>
          <button
            type="button"
            className="rounded-lg border border-slate-300 px-2 py-1 text-xs font-semibold hover:bg-slate-50"
            onClick={() => {
              setActiveConversationId('')
              setMessages([])
            }}
          >
            New
          </button>
        </div>

        {loadingConversations ? <LoadingBlock label="Loading conversations..." /> : null}
        {error ? <ErrorBlock message={error} /> : null}
        {error && lastFailedMessage ? (
          <button
            type="button"
            onClick={() => void onRetry()}
            className="mt-2 w-full rounded-lg border border-sky-300 px-3 py-2 text-xs font-semibold text-sky-800 hover:bg-sky-50"
          >
            Retry last message
          </button>
        ) : null}

        <ul className="grid gap-2">
          {conversations.map((conversation) => (
            <li key={conversation.conversationId}>
              <button
                type="button"
                onClick={() => void selectConversation(conversation.conversationId)}
                className={`w-full rounded-lg border px-3 py-2 text-left text-sm ${activeConversationId === conversation.conversationId ? 'border-sky-300 bg-sky-50 text-sky-800' : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'}`}
              >
                <p className="truncate font-semibold">{conversation.conversationId}</p>
                <p className="text-xs text-slate-500">{formatDateTime(conversation.updatedAt)}</p>
              </button>
            </li>
          ))}
        </ul>
      </aside>

      <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <div className="mb-4 h-[420px] space-y-3 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50 p-3">
          {messages.length === 0 ? (
            <EmptyBlock
              title="No messages yet"
              subtitle="Ask AeroMate about policies, flights, or your bookings."
            />
          ) : (
            messages.map((message, index) => (
              <article
                key={`${message.role}-${index}`}
                className={`max-w-[85%] rounded-xl px-3 py-2 text-sm ${message.role.toLowerCase() === 'user' ? 'ml-auto bg-sky-700 text-white' : 'bg-white text-slate-800 shadow-sm'}`}
              >
                <p className="mb-1 text-[10px] font-bold uppercase tracking-wide opacity-70">{message.role}</p>
                <p className="whitespace-pre-wrap">{message.content}</p>
              </article>
            ))
          )}
        </div>

        <form className="flex gap-2" onSubmit={onSendMessage}>
          <input
            className="input flex-1"
            placeholder="Type your message to AeroMate"
            value={messageInput}
            onChange={(event) => setMessageInput(event.target.value)}
            maxLength={4000}
          />
          <button
            type="submit"
            disabled={sending}
            className="rounded-lg bg-sky-700 px-4 py-2 font-semibold text-white hover:bg-sky-600 disabled:opacity-70"
          >
            {sending ? 'Sending...' : 'Send'}
          </button>
        </form>
      </div>
    </section>
  )
}
