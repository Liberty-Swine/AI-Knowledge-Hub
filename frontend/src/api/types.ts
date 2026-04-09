export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface KnowledgeBase {
  id: string
  name: string
  description?: string
  createTime?: string
  updateTime?: string
}

export interface FileInfo {
  id: string
  kbId: string
  fileName: string
  fileSize?: number
  fileSuffix?: string
  filePath?: string
  chunkCount?: number
  status?: number
  errorMessage?: string
  createTime?: string
  updateTime?: string
}

export interface ChatSession {
  id: string
  kbId: string
  title?: string
  createTime?: string
  lastActiveTime?: string
}

export interface ChatAskRequest {
  kbId: string
  sessionId: string
  question: string
  topK?: number
}

export interface ChatCitation {
  documentId?: string
  fileName?: string
  chunkIndex?: number
  previewUrl?: string
}

export interface ChatAskResponse {
  answer: string
  citations: ChatCitation[]
}

