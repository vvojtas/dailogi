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
- **Description**: Register a new user account
- **Request Body**:
  ```json
  {
    "email": "string",
    "name": "string",
    "password": "string"
  }
  ```
- **Response Body**:
  ```json
  {
    "id": "long",
    "email": "string",
    "name": "string",
    "created_at": "timestamp"
  }
  ```
- **Success Codes**: 201 Created
- **Error Codes**: 400 Bad Request (validation error), 409 Conflict (email already exists)

#### Login User
- **Method**: POST
- **Path**: `/api/auth/login`
- **Description**: Authenticate user and get JWT token
- **Request Body**:
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- **Response Body**:
  ```json
  {
    "token": "string",
    "user": {
      "id": "long",
      "name": "string",
      "email": "string",
      "is_special_user": "boolean"
    }
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized (invalid credentials)

### Users

#### Get Current User
- **Method**: GET
- **Path**: `/api/users/current`
- **Description**: Get current authenticated user information
- **Response Body**:
  ```json
  {
    "id": "long",
    "name": "string",
    "email": "string",
    "is_special_user": "boolean",
    "created_at": "timestamp",
    "has_api_key": "boolean"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 401 Unauthorized (not authenticated)

### API Keys

#### Save OpenRouter API Key
- **Method**: PUT
- **Path**: `/api/users/current/api-key`
- **Description**: Save user's OpenRouter API key (encrypted in storage)
- **Request Body**:
  ```json
  {
    "api_key": "string"
  }
  ```
- **Response Body**:
  ```json
  {
    "message": "string",
    "has_api_key": "boolean"
  }
  ```
- **Success Codes**: 200 OK
- **Error Codes**: 400 Bad Request (invalid key format), 401 Unauthorized

#### Delete OpenRouter API Key
- **Method**: DELETE
- **Path**: `/api/users/current/api-key`
- **Description**: Remove user's OpenRouter API key
- **Response Body**:
  ```json
  {
    "message": "string",
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

#### Delete Character
- **Method**: DELETE
- **Path**: `/api/characters/{id}`
- **Description**: Delete a character
- **Response Body**:
  ```json
  {
    "message": "string"
  }
  ```
- **Success Codes**: 200 OK
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


#### Generate Dialogue
- **Method**: POST
- **Path**: `/api/dialogues/{id}/generate`
- **Description**: Start dialogue generation process
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
- **Response Body**:
  ```json
  {
    "id": "long",
    "status": "string"
  }
  ```
- **Success Codes**: 202 Accepted
- **Error Codes**: 400 Bad Request (already generated), 401 Unauthorized, 402 Payment Required (no API key), 403 Forbidden (not owner), 404 Not Found


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

The API will implement JWT (JSON Web Token) based authentication:

1. **Authentication Flow**:
   - Users register or login through the `/api/auth` endpoints
   - Upon successful authentication, the server returns a JWT token
   - The client includes this token in the Authorization header of subsequent requests

2. **Token Structure**:
   - Header: Algorithm and token type
   - Payload: User ID, name, is_special_user flag, expiration time
   - Signature: Signed with server secret

3. **Token Validation**:
   - All protected endpoints validate the JWT token
   - Expired tokens are rejected
   - Tokens with invalid signatures are rejected

4. **Authorization Logic**:
   - Regular users can only access their own resources
   - Global characters are accessible by all users
   - Special users can use the global API key
   - Character deletion is restricted if the character is used in any dialogue

5. **Implementation Details**:
   - Spring Security will be used for authentication and authorization
   - Token expiration set to 24 hours
   - Refresh token mechanism not included in MVP

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

2. **Character Usage Restriction**:
   - Prevents deletion of characters used in dialogues
   - Implemented via database constraints and API validation

3. **Special User Handling**:
   - Checks is_special_user flag during dialogue generation
   - Uses global API key if no user key is provided
   - Regular users must provide their own API key 