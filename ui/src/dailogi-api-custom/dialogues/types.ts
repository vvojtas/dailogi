// Types for Dialogue Stream Events

/**
 * Base interface for all dialogue stream events
 */
export interface BaseDialogueEvent {
  type: string;
}

/**
 * Initial event with dialogue metadata
 */
export interface DialogueStartEvent extends BaseDialogueEvent {
  type: "dialogue-start";
  dialogue_id: number;
  character_configs: {
    character_id: number;
    llm_id: number;
  }[];
  turn_count: number;
}

/**
 * Signals the beginning of a character's turn
 */
export interface CharacterStartEvent extends BaseDialogueEvent {
  type: "character-start";
  character_config: {
    character_id: number;
    llm_id: number;
  };
  id: string; // Event ID for reconnection support
}

/**
 * Character response being generated token by token
 */
export interface TokenEvent extends BaseDialogueEvent {
  type: "token";
  character_config: {
    character_id: number;
    llm_id: number;
  };
  token: string;
  id: string; // Event ID for reconnection support
}

/**
 * Signals end of current character's turn
 */
export interface CharacterCompleteEvent extends BaseDialogueEvent {
  type: "character-complete";
  character_id: number;
  token_count: number;
  id: string; // Event ID for reconnection support
}

/**
 * Signals completion of entire dialogue
 */
export interface DialogueCompleteEvent extends BaseDialogueEvent {
  type: "dialogue-complete";
  status: "completed";
  turn_count: number;
  id: string; // Event ID for reconnection support
}

/**
 * Sent when an error occurs
 */
export interface ErrorEvent extends BaseDialogueEvent {
  type: "error";
  message: string;
  recoverable: boolean;
  id: string; // Event ID for reconnection support
}

/**
 * Union type for all dialogue stream events
 */
export type DialogueEvent =
  | DialogueStartEvent
  | CharacterStartEvent
  | TokenEvent
  | CharacterCompleteEvent
  | DialogueCompleteEvent
  | ErrorEvent;
