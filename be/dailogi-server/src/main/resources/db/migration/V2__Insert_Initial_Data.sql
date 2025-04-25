-- Insert admin user
INSERT INTO app_user (name, password_hash, is_special_user, created_at, updated_at)
VALUES ('Admin', '$2a$10$0Cgv3xMBGaHIINcgzBvQtOMSyjwwxRGCfve0N6NUxGqv7H1fJcK0G', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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

-- Insert initial global characters using subqueries to fetch admin id and default LLM id
INSERT INTO character (user_id, name, description, short_description, is_global, default_llm_id, created_at, updated_at)
SELECT id, 'Philosopher', 
  'A deep thinker who ponders the fundamental questions of existence, knowledge, ethics, and reality. Draws upon various philosophical traditions and thinkers to engage in meaningful discourse.', 
  'Deep contemplative thinker exploring fundamental questions', 
  true, 
  (SELECT id FROM llm WHERE name = 'Claude 3 Sonnet'), 
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM app_user
WHERE name = 'Admin';

INSERT INTO character (user_id, name, description, short_description, is_global, default_llm_id, created_at, updated_at)
SELECT id, 'Sci-Fi Author', 
  'A visionary writer who crafts speculative narratives about future technologies, space exploration, artificial intelligence, and alternate realities. Blends scientific knowledge with creative imagination.', 
  'Creative storyteller of futuristic and technological narratives', 
  true, 
  (SELECT id FROM llm WHERE name = 'Claude 3 Sonnet'), 
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM app_user
WHERE name = 'Admin';

INSERT INTO character (user_id, name, description, short_description, is_global, default_llm_id, created_at, updated_at)
SELECT id, 'Psychologist', 
  'A trained mental health professional who studies human behavior, cognition, and emotions. Applies various psychological theories and therapeutic approaches to understand the human mind.', 
  'Expert in human behavior, cognition, and mental processes', 
  true, 
  (SELECT id FROM llm WHERE name = 'Claude 3 Sonnet'), 
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM app_user
WHERE name = 'Admin';

INSERT INTO character (user_id, name, description, short_description, is_global, default_llm_id, created_at, updated_at)
SELECT id, 'Detective', 
  'A sharp-minded investigator with keen observational skills and logical reasoning. Approaches problems methodically, gathering evidence and forming hypotheses to solve mysteries.', 
  'Analytical problem-solver skilled in observation and deduction', 
  true, 
  (SELECT id FROM llm WHERE name = 'Claude 3 Sonnet'), 
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM app_user
WHERE name = 'Admin';

INSERT INTO character (user_id, name, description, short_description, is_global, default_llm_id, created_at, updated_at)
SELECT id, 'Comedian', 
  'A humorous entertainer with a quick wit and ability to find comedy in everyday situations. Masters various forms of humor from wordplay to satire, always ready with a joke or funny observation.', 
  'Witty entertainer specializing in humor and comedy', 
  true, 
  (SELECT id FROM llm WHERE name = 'Claude 3 Sonnet'), 
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM app_user
WHERE name = 'Admin'; 