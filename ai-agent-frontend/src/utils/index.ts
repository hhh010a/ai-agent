const API_BASE_URL = 'http://localhost:8083/api'

export async function fetchSSE(
  url: string,
  options?: RequestInit
): Promise<ReadableStreamDefaultReader<string>> {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  })

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('Response body is null')
  }

  const decoder = new TextDecoder()
  const stream = new ReadableStream<string>({
    async start(controller) {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          controller.close()
          break
        }
        const chunk = decoder.decode(value, { stream: true })
        controller.enqueue(chunk)
      }
    },
  })

  return stream.getReader()
}

export function generateId(): string {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

export function formatTime(date: Date): string {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}