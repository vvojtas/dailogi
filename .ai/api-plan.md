# REST API Plan for d-AI-logi

## 1. Resources

- **Users**: Corresponds to the `User` table, manages user accounts and authentication
- **Characters**: Corresponds to the `Character` table, represents AI personalities created by users or predefined ones
- **LLMs**: Corresponds to the `LLM` table, represents available language models
- **Dialogues**: Corresponds to the `Dialogue` table, represents conversations between characters
- **DialogueMessages**: Corresponds to the `DialogueMessage` table, represents individual messages in dialogues
- **DialogueCharacterConfigs**: Corresponds to the `DialogueCharacterConfig` table, represents character configurations in dialogues
- **APIKeys**: Represents OpenRouter API keys stored by users

## 2. Endpoints

### Authentication

#### Register User
- **Method**: POST
- **Path**: `/api/auth/register`
- **Description**: Register a new user.
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string",
    "passwordConfirmation": "string"
  }
  ```
- **Response Body**:
  ```json
  {
    "id": "long",
    "username": "string",
    "createdAt": "timestamp"
  }
  ```
- **Success Codes**: 201 Created
- **Error Codes**: 400 Bad Request (validation error), 409 Conflict (username already exists)

#### Login User
- **Method**: POST
- **Path**: `/api/auth/login`
- **Description**: Authenticate user and receive JWT token.
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Response Body**:
  ```json
  {
    "accessToken": "string",
    "tokenType": "string", // e.g., "Bearer"
    "expiresIn": "long",
    "user": {
      "id": "long",
      "username": "string",
      "createdAt": "timestamp"
    }
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 400 Bad Request (invalid credentials)

#### Get Current User
- **Method**: GET
- **Path**: `/api/auth/me`
- **Description**: Get information about the currently authenticated user.
- **Request Body**: (None)
- **Response Body**:
  ```json
  {
    "id": "long",
    "username": "string",
    "createdAt": "timestamp"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized

### API Keys

#### Set or update OpenRouter API Key
- **Method**: PUT
- **Path**: `/api/users/current/api-key`
- **Description**: Sets or updates the OpenRouter API key for the authenticated user
- **Request Body**:
  ```json
  {
    "api_key": "string"
  }
  ```
- **Response Body**:
  ```json
  {
    "has_api_key": "boolean"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 400 Bad Request (invalid key format), 401 Unauthorized

#### Delete OpenRouter API Key
- **Method**: DELETE
- **Path**: `/api/users/current/api-key`
- **Description**: Deletes the OpenRouter API key for the authenticated user
- **Response Body**:
  ```json
  {
    "has_api_key": "boolean"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized

#### Check OpenRouter API Key Status
- **Method**: GET
- **Path**: `/api/users/current/api-key`
- **Description**: Checks if the authenticated user has an OpenRouter API key set
- **Response Body**:
  ```json
  {
    "has_api_key": "boolean"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized

### Characters

#### Get Characters
- **Method**: GET
- **Path**: `/api/characters`
- **Description**: Get characters (user's own and/or global)
- **Query Parameters**:
  - `include_global` (optional, default true): Include global characters
  - `page` (optional, default 0): Page number for pagination
  - `size` (optional, default 20): Page size for pagination
- **Response Body**:
  ```json
  {
    "content": [
      {
        "id": "long",
        "name": "string",
        "short_description": "string",
        "description": "string",
        "has_avatar": "boolean",
        "avatar_url": "string",
        "is_global": "boolean",
        "default_llm_id": "long",
        "created_at": "timestamp",
        "updated_at": "timestamp"
      }
    ],
    "page": "integer",
    "size": "integer",
    "total_elements": "long",
    "total_pages": "integer"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized

#### Get Character
- **Method**: GET
- **Path**: `/api/characters/{id}`
- **Description**: Get a specific character by ID
- **Response Body**:
  ```json
  {
    "id": "long",
    "name": "string",
    "short_description": "string",
    "description": "string",
    "has_avatar": "boolean",
    "avatar_url": "string",
    "is_global": "boolean",
    "default_llm_id": "long",
    "created_at": "timestamp",
    "updated_at": "timestamp"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized, 403 Forbidden (not owner of non-global character), 404 Not Found

#### Create Character
- **Method**: POST
- **Path**: `/api/characters`
- **Description**: Create a new character
- **Request Body**:
  ```json
  {
    "name": "string",
    "short_description": "string",
    "description": "string",
    "default_llm_id": "long (optional)"
  }
  ```
- **Response Body**:
  ```json
  {
    "id": "long",
    "name": "string",
    "short_description": "string",
    "description": "string",
    "has_avatar": "boolean",
    "avatar_url": "string",
    "is_global": "boolean",
    "default_llm_id": "long",
    "created_at": "timestamp",
    "updated_at": "timestamp"
  }
  ```
- **Success Codes**: 201 Created
- **Error Codes**: 400 Bad Request (validation error), 401 Unauthorized, 409 Conflict (name already exists for user), 422 Unprocessable Entity (character limit reached)

#### Update Character
- **Method**: PUT
- **Path**: `/api/characters/{id}`
- **Description**: Update an existing character
- **Request Body**:
  ```json
  {
    "name": "string",
    "short_description": "string",
    "description": "string",
    "default_llm_id": "long (optional)"
  }
  ```
- **Response Body**:
  ```json
  {
    "id": "long",
    "name": "string",
    "short_description": "string",
    "description": "string",
    "has_avatar": "boolean",
    "avatar_url": "string",
    "is_global": "boolean",
    "default_llm_id": "long",
    "created_at": "timestamp",
    "updated_at": "timestamp"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 400 Bad Request (validation error), 401 Unauthorized, 403 Forbidden (not owner or global character), 404 Not Found, 409 Conflict (name already exists)

#### Upload Character Avatar
- **Method**: POST
- **Path**: `/api/characters/{id}/avatar`
- **Description**: Upload or replace character avatar
- **Request Body**: Multipart form data with image file (JPG/PNG, max 1MB, 256x256px)
- **Response Body**:
  ```json
  {
    "id": "long",
    "has_avatar": "boolean",
    "avatar_url": "string"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 400 Bad Request (invalid image), 401 Unauthorized, 403 Forbidden (not owner), 404 Not Found

#### Get Character Avatar
- **Method**: GET
- **Path**: `/api/characters/{id}/avatar`
- **Description**: Get character's avatar image file.
- **Request Body**: (None)
- **Response Body**: Image file (`image/png`, `image/jpeg`, etc.)
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized, 403 Forbidden (if user cannot access character), 404 Not Found (character or avatar)

#### Delete Character Avatar
- **Method**: DELETE
- **Path**: `/api/characters/{id}/avatar`
- **Description**: Delete character's avatar.
- **Request Body**: (None)
- **Response Body**: (No body)
- **Success Codes**: 204 No Content
- **Error Codes**: 401 Unauthorized, 403 Forbidden (not owner), 404 Not Found

#### Delete Character
- **Method**: DELETE
- **Path**: `/api/characters/{id}`
- **Description**: Delete a character
- **Response Body**: (No body)
- **Success Codes**: 204 No Content
- **Error Codes**: 401 Unauthorized, 403 Forbidden (not owner or global character), 404 Not Found, 409 Conflict (character used in dialogues)

### LLMs

#### Get LLMs
- **Method**: GET
- **Path**: `/api/llms`
- **Description**: Get available language models
- **Response Body**:
  ```json
  [
    {
      "id": "long",
      "name": "string",
      "openrouter_identifier": "string"
    }
  ]
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized

### Dialogues

#### Get Dialogues
- **Method**: GET
- **Path**: `/api/dialogues`
- **Description**: Get user's dialogues
- **Query Parameters**:
  - `page` (optional, default 0): Page number
  - `size` (optional, default 20): Page size
- **Response Body**:
  ```json
  {
    "content": [
      {
        "id": "long",
        "name": "string",
        "scene_description": "string",
        "status": "string (COMPLETED/FAILED)",
        "created_at": "timestamp",
        "updated_at": "timestamp",
        "characters": [
          {
            "id": "long",
            "name": "string",
            "has_avatar": "boolean",
            "avatar_url": "string"
          }
        ]
      }
    ],
    "page": "integer",
    "size": "integer",
    "total_elements": "long",
    "total_pages": "integer"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized

#### Get Dialogue
- **Method**: GET
- **Path**: `/api/dialogues/{id}`
- **Description**: Get a specific dialogue with messages
- **Response Body**:
  ```json
  {
    "id": "long",
    "name": "string",
    "scene_description": "string",
    "status": "string (COMPLETED/FAILED)",
    "created_at": "timestamp",
    "updated_at": "timestamp",
    "character_configs": [
      {
        "character": {
          "id": "long",
          "name": "string",
          "short_description": "string",
          "has_avatar": "boolean",
          "avatar_url": "string"
        },
        "llm": {
          "id": "long",
          "name": "string"
        }
      }
    ],
    "messages": [
      {
        "id": "long",
        "turn_number": "integer",
        "character_id": "long",
        "content": "string"
      }
    ]
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized, 403 Forbidden (not owner), 404 Not Found

#### Stream Dialogue Generation
- **Method**: POST
- **Path**: `/api/dialogues/stream`
- **Description**: Start dialogue generation with real-time streaming using Server-Sent Events (SSE)
- **Request Body**:
  ```json
  {
    "scene_description": "string",
    "character_configs": [
      {
        "character_id": "long",
        "llm_id": "long"
      }
    ]
  }
  ```
- **Response Type**: `text/event-stream`
- **Event Types**:
  - `dialogue-start`: Initial event with dialogue metadata
    ```json
    {
      "dialogue_id": "long",
      "character_configs": [
        {
          "character_id": "long",
          "character_name": "string",
          "has_avatar": "boolean",
          "avatar_url": "string" 
        }
      ],
      "turn_count": 0
    }
    ```
  - `character-start`: Signals the beginning of a character's turn
    ```json
    {
      "character_config": {
        "character_id": "long",
        "llm_id": "long"
      },
      "id": "string" // Event ID for reconnection support
    }
    ```
  - `token`: Character response being generated token by token
    ```json
    {
      "character_config": {
        "character_id": "long",
        "llm_id": "long"
      },
      "token": "string",
      "id": "string" // Event ID for reconnection support
    }
    ```
  - `character-complete`: Signals end of current character's turn
    ```json
    {
      "character_id": "long",
      "token_count": "integer",
      "id": "string" // Event ID for reconnection support
    }
    ```
  - `dialogue-complete`: Signals completion of entire dialogue
    ```json
    {
      "status": "completed",
      "turn_count": "integer",
      "id": "string" // Event ID for reconnection support
    }
    ```
  - `error`: Sent when an error occurs
    ```json
    {
      "message": "string",
      "recoverable": "boolean",
      "id": "string" // Event ID for reconnection support
    }
    ```
- **Reconnection**: Client can reconnect with `Last-Event-ID` header for resuming stream
- **Success Codes**: 200 OK with streaming response
- **Error Codes**: 400 Bad Request (invalid configuration), 401 Unauthorized, 402 Payment Required (no API key), 409 Conflict (invalid state)

#### Resume Dialogue Stream
- **Method**: GET
- **Path**: `/api/dialogues/{id}/stream`
- **Description**: Resume streaming a dialogue that was previously started using SSE
- **Headers**:
  - `Last-Event-ID`: Optional, ID of the last event received before disconnection
- **Response Type**: `text/event-stream`
- **Event Types**: Same as `/api/dialogues/stream`
- **Behavior**:
  - When `Last-Event-ID` is provided, streaming resumes from that point
  - Without `Last-Event-ID`, returns a dialogue summary event followed by real-time updates
  - For completed dialogues, streams all existing messages and sends `dialogue-complete`
- **Success Codes**: 200 OK with streaming response
- **Error Codes**: 401 Unauthorized, 403 Forbidden (not owner), 404 Not Found (dialogue doesn't exist)

#### Delete Dialogue
- **Method**: DELETE
- **Path**: `/api/dialogues/{id}`
- **Description**: Delete a dialogue
- **Response Body**:
  ```json
  {
    "message": "string"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized, 403 Forbidden (not owner), 404 Not Found

## 3. Authentication and Authorization

## 4. Validation and Business Logic

### Resource Limits
1. **Character Limits**:
   - Each user is limited to 50 characters
   - Validation occurs in the API layer before database operations
   - Returns 422 Unprocessable Entity if limit exceeded

2. **Dialogue Limits**:
   - Each user is limited to 50 saved dialogues
   - Validation occurs in the API layer before database operations
   - Returns 422 Unprocessable Entity if limit exceeded

3. **Dialogue Turn Limits**:
   - Maximum 50 turns per dialogue
   - Enforced during dialogue generation process

### Input Validation

1. **Character Validation**:
   - Name: Required, unique per user
   - Description: Required, maximum 500 words
   - Short description: Required
   - Avatar: JPG/PNG format, max 1MB, 256x256px

2. **Dialogue Validation**:
   - Name: Required
   - Character configs: Required, 2-3 characters
   - Each character must have an assigned LLM

3. **API Key Validation**:
   - Valid format check before saving
   - Encrypted storage using AES-GCM

### Business Logic Implementation

1. **Dialogue Generation Process**:
   - Implemented as an asynchronous process
   - Round-robin character sequence (Character A → B → C → A...)
   - Uses OpenRouter API with appropriate character's LLM
   - For special users without an API key, uses global API key
   - Handles and logs OpenRouter API errors
   - Updates dialogue status to COMPLETED or FAILED based on outcome
   - Saves generated messages to the database

2. **Dialogue Streaming Process**:
   - Implemented using Server-Sent Events (SSE) for real-time updates
   - Maintains persistent connection until dialogue completion or error
   - Supports token-by-token streaming from LLM to frontend
   - Handles reconnection with idempotent event IDs to resume from disconnection points
   - Uses thread pool to handle concurrent dialogue generation sessions
   - Maps OpenRouter streaming API to SSE events
   - Manages dialogue state to ensure consistency during reconnections
   - Performs proper resource cleanup when connections are terminated

3. **Character Usage Restriction**:
   - Prevents deletion of characters used in dialogues
   - Implemented via database constraints and API validation

4. **Special User Handling**:
   - Checks is_special_user flag during dialogue generation
   - Uses global API key if no user key is provided
   - Regular users must provide their own API key 