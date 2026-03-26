import { useState, useEffect, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChatRoom, Message } from '../types'
import { generateId } from '../utils'
import './Chat.css'

const API_BASE_URL = 'http://localhost:8083/api'
const STORAGE_KEY_AGENT1 = 'agent1_chat_data'

interface StoredChatData {
  chatRooms: ChatRoom[]
  roomMessages: Record<string, Message[]>
  currentRoomId: string
}

interface MessageSegment {
  type: 'thinking' | 'tool' | 'text'
  content: string
}

function parseMessageContent(content: string): MessageSegment[] {
  const segments: MessageSegment[] = []

  const toolStartMarker = '工具名称：'
  // 匹配 step1:思考:、step2:思考: 等作为工具内容的结束标记
  const stepThinkingPattern = /step\s*\d*\s*[：:]\s*思考[：:]/i

  let remaining = content

  while (remaining.indexOf(toolStartMarker) !== -1) {
    const toolStartIndex = remaining.indexOf(toolStartMarker)

    // 处理工具名称之前的内容
    if (toolStartIndex > 0) {
      const beforeTool = remaining.slice(0, toolStartIndex)
      const trimmedBefore = beforeTool.trim()
      if (trimmedBefore) {
        if (trimmedBefore.startsWith('思考：')) {
          segments.push({ type: 'thinking', content: trimmedBefore })
        } else {
          segments.push({ type: 'text', content: trimmedBefore })
        }
      }
    }

    // 找到 stepX:思考: 作为工具内容的结束点
    let toolContent = remaining.slice(toolStartIndex)
    const stepThinkingMatch = stepThinkingPattern.exec(toolContent)

    let toolEndIndex = toolContent.length
    if (stepThinkingMatch && stepThinkingMatch.index > 0) {
      // 工具内容在 stepX:思考: 之前结束
      toolEndIndex = stepThinkingMatch.index
    }

    // 提取工具内容（不包括 stepX:思考: 及其后的内容）
    let toolContentOnly = toolContent.slice(0, toolEndIndex)

    const trimmedTool = toolContentOnly.trim()
    if (trimmedTool) {
      segments.push({ type: 'tool', content: trimmedTool })
    }

    // 继续处理剩余内容（从 stepX:思考: 开始）
    remaining = toolContent.slice(toolEndIndex)

    if (remaining.trim() === '') break
  }

  // 处理剩余的非工具内容
  if (remaining.trim()) {
    // 检查是否以 stepX:思考: 开头
    const stepThinkingPattern = /step\s*\d*\s*[：:]\s*思考[：:]/i
    const match = remaining.match(stepThinkingPattern)
    if (match && match.index === 0) {
      // 提取思考内容
      const thinkingContent = remaining.slice(match[0].length).trim()
      if (thinkingContent) {
        segments.push({ type: 'thinking', content: thinkingContent })
      }
    } else {
      segments.push({ type: 'text', content: remaining.trim() })
    }
  }

  return segments
}

function renderMessageContent(content: string, isStreaming: boolean) {
  if (isStreaming) {
    return <span>{content}</span>
  }

  const segments = parseMessageContent(content)

  return segments.map((segment, index) => {
    if (segment.type === 'tool') {
      return (
        <pre key={index} className="tool-call">
          {segment.content}
        </pre>
      )
    }
    return (
      <span key={index} className={segment.type === 'thinking' ? 'thinking-text' : ''}>
        {segment.content}
      </span>
    )
  })
}

function Agent1Chat() {
  const navigate = useNavigate()
  const [chatRooms, setChatRooms] = useState<ChatRoom[]>([])
  const [currentRoomId, setCurrentRoomId] = useState<string>('')
  const [roomMessages, setRoomMessages] = useState<Record<string, Message[]>>({})
  const [inputValue, setInputValue] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  const messages = roomMessages[currentRoomId] || []

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [])

  useEffect(() => {
    scrollToBottom()
  }, [messages, scrollToBottom])

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY_AGENT1)
    if (stored) {
      try {
        const data: StoredChatData = JSON.parse(stored)
        setChatRooms(data.chatRooms)
        setCurrentRoomId(data.currentRoomId)
        const messagesWithDates = Object.fromEntries(
          Object.entries(data.roomMessages).map(([k, v]) => [
            k,
            v.map((m) => ({ ...m, timestamp: new Date(m.timestamp) })),
          ])
        )
        setRoomMessages(messagesWithDates)
      } catch {
        const initialRoom: ChatRoom = {
          id: generateId(),
          name: `聊天 1`,
          createdAt: new Date(),
        }
        setChatRooms([initialRoom])
        setCurrentRoomId(initialRoom.id)
        setRoomMessages({ [initialRoom.id]: [] })
      }
    } else {
      const initialRoom: ChatRoom = {
        id: generateId(),
        name: `聊天 1`,
        createdAt: new Date(),
      }
      setChatRooms([initialRoom])
      setCurrentRoomId(initialRoom.id)
      setRoomMessages({ [initialRoom.id]: [] })
    }
  }, [])

  useEffect(() => {
    if (chatRooms.length > 0 && currentRoomId) {
      const data: StoredChatData = { chatRooms, roomMessages, currentRoomId }
      localStorage.setItem(STORAGE_KEY_AGENT1, JSON.stringify(data))
    }
  }, [chatRooms, roomMessages, currentRoomId])

  const createNewChatRoom = () => {
    const newRoom: ChatRoom = {
      id: generateId(),
      name: `聊天 ${chatRooms.length + 1}`,
      createdAt: new Date(),
    }
    setChatRooms([...chatRooms, newRoom])
    setCurrentRoomId(newRoom.id)
    setRoomMessages((prev) => ({ ...prev, [newRoom.id]: [] }))
  }

  const selectChatRoom = (roomId: string) => {
    setCurrentRoomId(roomId)
  }

  const sendMessage = async () => {
    if (!inputValue.trim() || isStreaming || !currentRoomId) return

    const userMessage: Message = {
      id: generateId(),
      role: 'user',
      content: inputValue.trim(),
      timestamp: new Date(),
    }

    setRoomMessages((prev) => ({
      ...prev,
      [currentRoomId]: [...(prev[currentRoomId] || []), userMessage],
    }))
    setInputValue('')
    setIsStreaming(true)

    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto'
    }

    const assistantMessageId = generateId()
    setRoomMessages((prev) => ({
      ...prev,
      [currentRoomId]: [
        ...(prev[currentRoomId] || []),
        {
          id: assistantMessageId,
          role: 'assistant',
          content: '',
          timestamp: new Date(),
        },
      ],
    }))

    try {
      const response = await fetch(
        `${API_BASE_URL}/ai/agent1/chat/sse?userInput=${encodeURIComponent(userMessage.content)}`
      )

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('Response body is null')
      }

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { value, done } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const content = line.slice(5).trim()
            if (content) {
              setRoomMessages((prev) => ({
                ...prev,
                [currentRoomId]: prev[currentRoomId].map((msg) =>
                  msg.id === assistantMessageId
                    ? { ...msg, content: msg.content + content }
                    : msg
                ),
              }))
            }
          }
        }
      }
    } catch (error) {
      console.error('SSE error:', error)
      setRoomMessages((prev) => ({
        ...prev,
        [currentRoomId]: prev[currentRoomId].map((msg) =>
          msg.id === assistantMessageId
            ? { ...msg, content: '抱歉，发生了错误，请重试。' }
            : msg
        ),
      }))
    } finally {
      setIsStreaming(false)
      inputRef.current?.focus()
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInputValue(e.target.value)
    const textarea = e.target
    textarea.style.height = 'auto'
    textarea.style.height = `${Math.min(textarea.scrollHeight, 150)}px`
  }

  return (
    <div className="chat-layout">
      <aside className="chat-sidebar">
        <div className="sidebar-header">
          <button className="back-btn" onClick={() => navigate('/')}>
            ←
          </button>
          <h1 className="sidebar-title">AI Agent</h1>
        </div>

        <button className="new-chat-btn" onClick={createNewChatRoom}>
          + 新建聊天
        </button>

        <div className="chat-rooms">
          {chatRooms.map((room) => (
            <div
              key={room.id}
              className={`chat-room-item ${currentRoomId === room.id ? 'active' : ''}`}
              onClick={() => selectChatRoom(room.id)}
            >
              <span className="chat-room-name">{room.name}</span>
            </div>
          ))}
        </div>
      </aside>

      <main className="chat-main">
        {messages.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🤖</div>
            <p className="empty-state-text">开始和 AI Agent 对话吧</p>
          </div>
        ) : (
          <div className="chat-messages">
            {messages.map((message) => (
              <div key={message.id} className={`message ${message.role}`}>
                <div className="message-avatar">
                  {message.role === 'user' ? '👤' : '🤖'}
                </div>
                <div className="message-content">
                  {renderMessageContent(message.content, message.role === 'assistant' && isStreaming && message.content === '')}
                  {message.role === 'assistant' && isStreaming && message.content === '' && (
                    <div className="typing-indicator">
                      <span className="typing-dot" />
                      <span className="typing-dot" />
                      <span className="typing-dot" />
                    </div>
                  )}
                </div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>
        )}

        <div className="chat-input-area">
          <div className="chat-input-wrapper">
            <textarea
              ref={textareaRef}
              className="chat-input"
              value={inputValue}
              onChange={handleInputChange}
              onKeyDown={handleKeyDown}
              placeholder="输入你的问题..."
              rows={1}
            />
            <button
              className="send-btn"
              onClick={sendMessage}
              disabled={!inputValue.trim() || isStreaming}
            >
              ↑
            </button>
          </div>
        </div>
      </main>
    </div>
  )
}

export default Agent1Chat