export interface ChatRoom {
  id: string
  name: string
  createdAt: Date
}

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}