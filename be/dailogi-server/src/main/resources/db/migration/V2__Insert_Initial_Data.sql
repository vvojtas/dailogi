-- Insert admin user
INSERT INTO app_user (name, password_hash, is_special_user, created_at, updated_at)
VALUES ('Admin', '$2a$10$FHgT8jWB9NxIKt4XmkZOu.PHi.X4O/FOdob713lunc7B8xafVAmra', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert LLM entries
INSERT INTO llm (name, openrouter_identifier)
VALUES
  ('GPT-3.5 Turbo', 'openai/gpt-3.5-turbo'),
  ('GPT-4', 'openai/gpt-4'),
  ('Claude 3 Opus', 'anthropic/claude-3-opus'),
  ('Claude 3 Sonnet', 'anthropic/claude-3-sonnet'),
  ('Claude 3 Haiku', 'anthropic/claude-3-haiku'),
  ('Llama 3 70B', 'meta-llama/llama-3-70b-instruct'),
  ('Mistral Medium', 'mistralai/mistral-medium');