-- Insert admin user
INSERT INTO app_user (name, password_hash, is_special_user, created_at, updated_at)
VALUES ('Admin', '$2a$10$FHgT8jWB9NxIKt4XmkZOu.PHi.X4O/FOdob713lunc7B8xafVAmra', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert LLM entries
INSERT INTO llm (name, openrouter_identifier)
VALUES
  ('GPT-4.1', 'openai/gpt-4.1'),
  ('Claude 3.7 Sonnet', 'anthropic/claude-3.7-sonnet'),
  ('Llama 3 70B', 'meta-llama/llama-3-70b-instruct'),
  ('Mistral Medium 3', 'mistralai/mistral-medium-3'),
  ('DeepSeek V3 0324', 'deepseek/deepseek-chat-v3-0324');