import { useEffect, useMemo, useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { useLocation } from 'react-router-dom'
import { chatbotApi } from '@/api/chatbot'
import { useAuth } from '@/auth/AuthContext'
import './ai-chat-widget.css'

type ChatRole = 'assistant' | 'user'

interface ChatMessage {
  id: string
  role: ChatRole
  text: string
}

const QUICK_QUESTIONS = [
  'How do the current reward rules work?',
  'What is my total play time today?',
  'What is my total prize reward today?',
]

const WELCOME_TEXT =
  'Hi! I can explain reward configurations and check your listening time or prize rewards. What would you like to know?'

function createId() {
  return typeof crypto !== 'undefined' && 'randomUUID' in crypto
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function getSessionId(userId: number) {
  const key = `reward.chat.session.${userId}`
  const existing = sessionStorage.getItem(key)
  if (existing) return existing
  const created = createId()
  sessionStorage.setItem(key, created)
  return created
}

function RobotIcon() {
  return (
    <svg viewBox="0 0 32 32" aria-hidden="true">
      <path d="M16 3v4" />
      <circle cx="16" cy="3" r="1.5" />
      <rect x="6" y="8" width="20" height="17" rx="6" />
      <path d="M10 25v3m12-3v3M6 14H3v6h3m20-6h3v6h-3" />
      <circle cx="12" cy="16" r="1.5" className="ai-support__icon-fill" />
      <circle cx="20" cy="16" r="1.5" className="ai-support__icon-fill" />
      <path d="M12 21h8" />
    </svg>
  )
}

export default function AIChatWidget() {
  const { auth } = useAuth()
  const location = useLocation()
  const [open, setOpen] = useState(false)
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const endRef = useRef<HTMLDivElement>(null)
  const activeUserIdRef = useRef(auth?.userId)

  const sessionId = useMemo(() => (auth ? getSessionId(auth.userId) : ''), [auth?.userId])
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: createId(),
      role: 'assistant',
      text: WELCOME_TEXT,
    },
  ])

  useEffect(() => {
    if (open) endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading, open])

  // Never carry one user's on-screen conversation into another user's session.
  useEffect(() => {
    activeUserIdRef.current = auth?.userId
    setOpen(false)
    setInput('')
    setLoading(false)
    setError(null)
    setMessages([{ id: createId(), role: 'assistant', text: WELCOME_TEXT }])
  }, [auth?.userId])

  // The assistant is user-specific and should not cover the reward admin interface.
  if (!auth || location.pathname.startsWith('/admin')) return null

  const sendMessage = async (rawInput: string) => {
    const userInput = rawInput.trim()
    if (!userInput || loading) return
    const requestUserId = auth.userId

    setMessages((current) => [...current, { id: createId(), role: 'user', text: userInput }])
    setInput('')
    setError(null)
    setLoading(true)

    try {
      const response = await chatbotApi.chat(sessionId, userInput)
      if (activeUserIdRef.current !== requestUserId) return
      if (!response.success || !response.data?.trim()) {
        throw new Error(response.errorMessage || 'The assistant could not answer this question.')
      }
      setMessages((current) => [
        ...current,
        { id: createId(), role: 'assistant', text: response.data!.trim() },
      ])
    } catch (requestError) {
      if (activeUserIdRef.current !== requestUserId) return
      const message = requestError instanceof Error && requestError.message !== 'Network Error'
        ? requestError.message
        : 'The assistant is temporarily unavailable. Please try again.'
      setError(message)
    } finally {
      if (activeUserIdRef.current === requestUserId) setLoading(false)
    }
  }

  const submit = (event: FormEvent) => {
    event.preventDefault()
    void sendMessage(input)
  }

  const handleKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void sendMessage(input)
    }
  }

  return (
    <div className={`ai-support${open ? ' ai-support--open' : ''}`}>
      {open && (
        <section className="ai-support__panel" aria-label="AI customer support">
          <header className="ai-support__header">
            <span className="ai-support__header-icon"><RobotIcon /></span>
            <span className="ai-support__heading">
              <strong>Reward Assistant</strong>
              <small><i aria-hidden="true" /> Online · Ask about your rewards</small>
            </span>
            <button
              type="button"
              className="ai-support__close"
              onClick={() => setOpen(false)}
              aria-label="Close AI support"
            >
              ×
            </button>
          </header>

          <div className="ai-support__messages" aria-live="polite">
            {messages.map((message) => (
              <div key={message.id} className={`ai-support__message ai-support__message--${message.role}`}>
                {message.role === 'assistant' && (
                  <span className="ai-support__avatar" aria-hidden="true"><RobotIcon /></span>
                )}
                <p>{message.text}</p>
              </div>
            ))}

            {messages.length === 1 && (
              <div className="ai-support__quick" aria-label="Suggested questions">
                {QUICK_QUESTIONS.map((question) => (
                  <button key={question} type="button" onClick={() => void sendMessage(question)}>
                    {question}
                  </button>
                ))}
              </div>
            )}

            {loading && (
              <div className="ai-support__message ai-support__message--assistant">
                <span className="ai-support__avatar" aria-hidden="true"><RobotIcon /></span>
                <span className="ai-support__typing" aria-label="Assistant is thinking">
                  <i /><i /><i />
                </span>
              </div>
            )}
            {error && (
              <div className="ai-support__error" role="alert">
                <span>{error}</span>
                <button type="button" onClick={() => void sendMessage(messages.at(-1)?.text ?? '')}>
                  Try again
                </button>
              </div>
            )}
            <div ref={endRef} />
          </div>

          <form className="ai-support__composer" onSubmit={submit}>
            <textarea
              rows={1}
              value={input}
              onChange={(event) => setInput(event.target.value)}
              onKeyDown={handleKeyDown}
              maxLength={500}
              placeholder="Ask about your listening rewards..."
              aria-label="Message AI support"
              disabled={loading}
            />
            <button type="submit" disabled={!input.trim() || loading} aria-label="Send message">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="m4 4 17 8-17 8 3-8-3-8Z" />
                <path d="M7 12h14" />
              </svg>
            </button>
          </form>
          <small className="ai-support__notice">AI answers use your current RewardProject data.</small>
        </section>
      )}

      {!open && <span className="ai-support__hint">click me to support your questions</span>}
      <button
        type="button"
        className="ai-support__launcher"
        onClick={() => setOpen((current) => !current)}
        aria-label={open ? 'Close AI support' : 'Open AI support'}
        aria-expanded={open}
      >
        {open ? <span aria-hidden="true">×</span> : <RobotIcon />}
      </button>
    </div>
  )
}
