import React, { useEffect, useState } from 'react';
import icon from './icon.png';

const apiFetch = async (path, options = {}) => {
  console.log(`API request: ${options.method || 'GET'} ${path}`);
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  if (!response.ok) {
    throw new Error(`API error ${response.status}`);
  }
  return response.json();
};

export default function App() {
  const [conversations, setConversations] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [search, setSearch] = useState('');
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [prompt, setPrompt] = useState('');
  const [newTitle, setNewTitle] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadConversations();
  }, []);

  useEffect(() => {
    if (!search) return setFiltered(conversations);
    const q = search.toLowerCase();
    setFiltered(conversations.filter(c => (c.title || '').toLowerCase().includes(q)));
  }, [search, conversations]);

  const loadConversations = async () => {
    try {
      const data = await apiFetch('/conversations');
      setConversations(data);
      setFiltered(data);
    } catch (err) {
      setError('Unable to load conversations.');
    }
  };

  const loadConversation = async (conversationId) => {
    try {
      setIsLoading(true);
      const data = await apiFetch(`/conversations/${conversationId}/messages`);
      setSelectedConversation({
        id: conversationId,
        messages: data,
      });
      setPrompt('');
      setError('');
    } catch (err) {
      setError('Unable to load conversation history.');
    } finally {
      setIsLoading(false);
    }
  };

  const createConversation = async () => {
    try {
      setIsLoading(true);
      const data = await apiFetch('/conversations', {
        method: 'POST',
        body: JSON.stringify({ title: newTitle })
      });
      setNewTitle('');
      await loadConversations();
      setSelectedConversation(data);
      setError('');
    } catch (err) {
      setError('Unable to create conversation.');
    } finally {
      setIsLoading(false);
    }
  };

  const sendMessage = async () => {
    if (!selectedConversation || !prompt.trim()) return;
    setSelectedConversation((current) => ({
        ...current,
        messages: [...(current.messages || []), { content: prompt.trim(), role: 'USER', createdAt: new Date(Date.now()).toLocaleString(), id: `temp-${Date.now()}` }]
      }));

    try {
      setIsLoading(true);
      const message = await apiFetch(`/conversations/${selectedConversation.id}/messages`, {
        method: 'POST',
        body: JSON.stringify({ content: prompt.trim() })
      });

      setSelectedConversation((current) => ({
        ...current,
        messages: [...(current.messages || []), message]
      }));
      setPrompt('');
      setError('');
    } catch (err) {
      setError('Unable to send message.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-icon"><img src={icon} alt="toto" /></div>
          <div className="brand-title">AI Companion</div>
        </div>

        <div className="sidebar-controls">
          <input
            className="search"
            value={search}
            placeholder="Search conversations..."
            onChange={(e) => setSearch(e.target.value)}
          />

          <div className="new-conversation">
            <input
              type="text"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              placeholder="New conversation title"
            />
            <button onClick={createConversation} disabled={isLoading}>
              +
            </button>
          </div>
        </div>

        <div className="conversation-list">
          {filtered.length === 0 && <p className="empty-state">No conversations yet.</p>}
          {filtered.map((conversation) => (
            <button
              key={conversation.id}
              className={`conversation-item ${selectedConversation?.id === conversation.id ? 'active' : ''}`}
              onClick={() => loadConversation(conversation.id)}
            >
              <div className="conv-left">
                <div className="conv-avatar">{(conversation.title && conversation.title[0]) || 'C'}</div>
                <div className="conv-meta">
                  <div className="conversation-title">{conversation.title}</div>
                  <div className="conversation-snippet">{conversation.createdAt ? new Date(conversation.createdAt).toLocaleString() : ''}</div>
                </div>
              </div>
            </button>
          ))}
        </div>
      </aside>

      <main className="main-panel">
        {error && <div className="error-banner">{error}</div>}

        {!selectedConversation ? (
          <div className="empty-view">
            <h2>Select a conversation</h2>
            <p>Click on a conversation from the left sidebar or create a new one.</p>
          </div>
        ) : (
          <>
            <header className="conversation-header">
              <div>
                <h2>{selectedConversation.title}</h2>
                <p className="conversation-subtitle">{selectedConversation.messages?.length ?? 0} messages</p>
              </div>
            </header>

            <section className="message-history">
              {selectedConversation.messages?.length === 0 && (
                <div className="empty-state">No messages yet. Start the conversation below.</div>
              )}

              {selectedConversation.messages?.map((message) => (
                <div key={message.id} className={`message-row ${message.role.toLowerCase()}`}>
                  <div className="message-avatar">{message.role === 'USER' ? 'U' : 'AI'}</div>
                  <div className="message-body">
                    <div className="message-header">
                      <div className="message-role">{message.role}</div>
                      <div className="message-time">{message.createdAt ? new Date(message.createdAt).toLocaleTimeString() : ''}</div>
                    </div>
                    <div className="message-content">{message.content}</div>
                  </div>
                </div>
              ))}

            </section>

            <footer className="composer">
              <textarea
                rows="3"
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                placeholder="Type your prompt here..."
              />

              <div className="composer-actions">
                <button className="attach">📎</button>
                <button className="send" onClick={sendMessage} disabled={isLoading || !prompt.trim()}>
                  {isLoading ? 'Sending…' : 'Send'}
                </button>
              </div>
            </footer>
          </>
        )}
      </main>
    </div>
  );
}
