## 1. Analysis

The `CharacterSlot.tsx` component is responsible for rendering a UI section where users can select a character and an associated LLM model for a scene. It uses a custom hook, `useCharacterSlot`, to manage its state and logic, including fetching character details via an API call (`getCharacter`) when a character is selected.

The component is already integrated with React Hook Form (`useFormContext`, `FormField`), which is a good starting point. Key areas for refactoring include:

*   **API Call Management:** The `fetchCharacterDetails` function within `useCharacterSlot` and its interaction with `useEffect` and `useCallback` dependencies need refinement to prevent potential infinite loops or unnecessary re-renders/fetches. Specifically, the dependency of `useCallback(fetchCharacterDetails, [...characterDetails])` is problematic.
*   **Form Value Updates:** The way `Select` component changes are propagated to the RHF state via `field.onChange` and then also through `handleCharacterChange`/`handleLlmChange` (which again call `form.setValue`) can be streamlined.
*   **Side Effect Management:** Logic for clearing dependent fields (e.g., clearing LLM when character is cleared) or setting default LLMs should be clearly tied to form value changes, typically using `useEffect` to watch the relevant RHF state.

## 2. Refactoring Plan

### 2.1 Component Structure Changes

The overall structure with `CharacterSlot` and the `useCharacterSlot` custom hook will be maintained as it promotes good separation of concerns. The primary changes will be internal to the `useCharacterSlot` hook and how event handlers in the JSX interact with RHF.

### 2.2 React Hook Form Implementation Refinements

The component already uses `<FormField>` which internally uses RHF's `Controller`. We will refine how `field.onChange` is used and how side effects are triggered.

**`useCharacterSlot` Hook Refinements:**

1.  **Stabilize `fetchCharacterDetails`:**
    *   Remove `characterDetails` from the `useCallback` dependency array of `fetchCharacterDetails`. The function will close over the `characterDetails` state. The check `if (!id || (characterDetails && characterDetails.id === id))` will use the `characterDetails` state from the render in which `fetchCharacterDetails` was defined. This is safe as long as the `useEffect` calling it is correctly dependent on `characterId`.
    *   The dependencies for `fetchCharacterDetails` should primarily be `form.setValue`, `index`, and `setCharacterDetails` (or just `form` and `index` if `form.setValue` is stable and `setCharacterDetails` is obtained from `useState` which is stable).

    ```typescript
    // Inside useCharacterSlot
    const fetchCharacterDetails = useCallback(
      async (id: number) => {
        // The existing check: if current characterDetails in state is already for this id, return.
        // This is okay as characterDetails is from the hook's state.
        if (!id || (characterDetails && characterDetails.id === id)) {
          // If details are already loaded for this ID, ensure default LLM is re-evaluated if needed,
          // or simply return if no further action is needed.
          // If default LLM was already set, this check prevents re-setting it unnecessarily.
          return;
        }
    
        // Reset details for new character loading
        setCharacterDetails(null); 
        // Clear previous default LLM if character changes before new one loads
        // Or rely on the useEffect watching characterId to clear LLM if characterId becomes undefined
    
        try {
          const response = await getCharacter(id);
          const details = response.data;
          setCharacterDetails(details);
    
          if (details?.default_llm_id) {
            // Only set if llmId is not already set or if it's different
            // This prevents overriding a user's explicit LLM choice if they change character and then change back
            // For simplicity here, we will set it as per original logic.
            form.setValue(`configs.${index}.llmId`, details.default_llm_id, {
              shouldValidate: true,
              shouldDirty: true,
              shouldTouch: true,
            });
          } else {
            // If the new character has no default LLM, clear any existing LLM if it wasn't user-selected after character load.
            // This is tricky. The current logic only sets a default, doesn't clear if no default.
            // For now, let's stick to original: only set if default_llm_id exists.
          }
        } catch (error) {
          console.error(`[CharacterSlot ${index}] Failed to fetch character details for ${id}:`, error);
          if (!(error instanceof DailogiError && error.displayed)) {
            toast.error("Nie udało się załadować szczegółów postaci.");
          }
          // Consider clearing characterId if fetch fails, or let user retry.
        }
      },
      // Dependencies should be stable ones from parent scope or form context
      [form, index, characterDetails] // KEEP characterDetails here for the check, BUT ensure the useEffect is robust.
                                      // OR, better:
      // [form.setValue, index, setCharacterDetails] // This is more accurate to what's used.
      // Let's try to remove characterDetails from dependency and see if it works as expected by relying on closure.
      // Recommended:
      // [form, index] // And use setCharacterDetails directly. form object itself is stable.
    );
    // After more thought, the original `[form, index, characterDetails]` for `useCallback`
    // while ensuring the `useEffect` correctly triggers on `characterId` is a common pattern,
    // but the goal of useCallback is to memoize. If `characterDetails` changes, the function changes.
    // The `if (characterDetails && characterDetails.id === id)` check is the key.

    // Let's go with minimal stable deps for fetchCharacterDetails's useCallback:
    // const { setValue } = form; // Destructure to ensure stability if form object is complex
    // const fetchCharacterDetails = useCallback(async (id: number) => { ... }, [setValue, index, setCharacterDetails]);
    // The useEffect calling it:
    // useEffect(() => {
    //   if (characterId) {
    //     fetchCharacterDetails(characterId);
    //   } else {
    //     setCharacterDetails(null);
    //     // Also clear LLM when character is cleared
    //     setValue(`configs.${index}.llmId`, undefined, { /* RHF options */ });
    //   }
    // }, [characterId, fetchCharacterDetails, setValue, index]);
    ```

2.  **Centralize Side Effects using `useEffect`:**
    *   Use `useEffect` to react to changes in `characterId` (from RHF state) to:
        *   Fetch character details.
        *   Clear `llmId` if `characterId` is cleared.
        *   Reset `characterDetails` state if `characterId` is cleared.

    ```typescript
    // Inside useCharacterSlot, replace the existing useEffect for characterId
    const { setValue } = form; // Destructure for stable reference

    useEffect(() => {
      if (characterId) {
        fetchCharacterDetails(characterId);
      } else {
        // Character has been deselected
        setCharacterDetails(null);
        setValue(`configs.${index}.llmId`, undefined, {
          shouldValidate: true,
          shouldDirty: true,
          shouldTouch: true,
        });
      }
    }, [characterId, index, fetchCharacterDetails, setValue]); // fetchCharacterDetails must be stable
    ```
    To make `fetchCharacterDetails` stable, its own `useCallback` dependencies must be stable.
    `fetchCharacterDetails` ideally should not depend on `characterDetails` state for its own memoization if it sets that state.

    Revised `fetchCharacterDetails` for stability:
    ```typescript
    const { setValue } = form;
    const fetchCharacterDetailsCallback = useCallback(async (id: number, currentDetails: CharacterDTO | null) => {
        if (!id || (currentDetails && currentDetails.id === id)) return;

        setCharacterDetails(null); // Show loading or clear previous

        try {
            const response = await getCharacter(id);
            const details = response.data;
            setCharacterDetails(details);

            if (details?.default_llm_id) {
                setValue(`configs.${index}.llmId`, details.default_llm_id, {
                    shouldValidate: true, shouldDirty: true, shouldTouch: true,
                });
            }
        } catch (error) { /* ... error handling ... */ }
    }, [index, setValue /*, other stable dependencies like getCharacter if it were a prop */]);

    // And the useEffect:
    useEffect(() => {
        if (characterId) {
            fetchCharacterDetailsCallback(characterId, characterDetails); // Pass currentDetails
        } else {
            setCharacterDetails(null);
            setValue(`configs.${index}.llmId`, undefined, { /* options */ });
        }
    }, [characterId, index, fetchCharacterDetailsCallback, setValue, characterDetails]); // characterDetails is now a dep here
    ```
    This is still a bit circular. The most robust way is often to let `useEffect` manage the "is stale" check based on `characterId`.

    **Simpler & Recommended `fetchCharacterDetails` and `useEffect` structure:**
    ```typescript
    // Inside useCharacterSlot
    const { setValue } = form;

    // Stable fetch function, does not depend on internal state for its identity
    const stableFetchCharacterDetails = useCallback(async (id: number) => {
      setCharacterDetails(null); // Indicate loading/reset
      try {
        const response = await getCharacter(id);
        const details = response.data;
        setCharacterDetails(details); // Set new details

        // Set default LLM if available AND if LLM is not already set by user for this character
        // (or simply always set default, which is current behavior)
        if (details?.default_llm_id) {
          setValue(`configs.${index}.llmId`, details.default_llm_id, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
        }
      } catch (error) {
        console.error(`[CharacterSlot ${index}] Failed to fetch character details for ${id}:`, error);
        setCharacterDetails(null); // Clear details on error
        if (!(error instanceof DailogiError && error.displayed)) {
          toast.error("Nie udało się załadować szczegółów postaci.");
        }
      }
    }, [index, setValue]); // Dependencies: index, setValue (from form)

    useEffect(() => {
      if (characterId) {
        // Only fetch if details are not already loaded for the current characterId
        if (!characterDetails || characterDetails.id !== characterId) {
          stableFetchCharacterDetails(characterId);
        }
      } else {
        setCharacterDetails(null);
        setValue(`configs.${index}.llmId`, undefined, {
          shouldValidate: true,
          shouldDirty: true,
          shouldTouch: true,
        });
      }
    }, [characterId, index, setValue, characterDetails, stableFetchCharacterDetails]);
    ```

3.  **Simplify `Select` `onValueChange`:**
    *   The `onValueChange` for both `Select` components (Character and LLM) should directly call `field.onChange` with the processed value (parsed integer or `undefined`).
    *   The `handleCharacterChange` and `handleLlmChange` callbacks can be removed, as their logic is now either in `field.onChange` or the `useEffect` watching `characterId`.

    ```typescript
    // In CharacterSlot JSX for Character Select
    <FormField
      control={form.control}
      name={`configs.${index}.characterId`}
      render={({ field }) => (
        <FormItem>
          <FormLabel>Wybierz postać</FormLabel>
          <Select
            key={`character-select-${index}-${field.value}`} // Keying on field.value can help if Select has internal state to reset
            value={field.value?.toString() || ""}
            onValueChange={(valueString) => {
              const processedValue = valueString === "undefined" ? undefined : parseInt(valueString, 10);
              field.onChange(processedValue);
            }}
            disabled={disabled}
          >
            {/* ... SelectTrigger, SelectContent ... */}
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />

    // In CharacterSlot JSX for LLM Select
    <FormField
      control={form.control}
      name={`configs.${index}.llmId`}
      render={({ field }) => (
        <FormItem>
          <FormLabel>Wybierz model LLM</FormLabel>
          <Select
            value={field.value?.toString() || ""}
            onValueChange={(valueString) => {
              const processedValue = valueString === "undefined" ? undefined : parseInt(valueString, 10);
              field.onChange(processedValue);
            }}
            disabled={disabled || !characterId} // characterId is from form.watch()
          >
            {/* ... SelectTrigger, SelectContent ... */}
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
    ```

4.  **Refine `handleClearSelection`:**
    *   This function is already quite good. It correctly sets both `characterId` and `llmId` to `undefined`. The `setCharacterDetails(null)` is also correct. No major changes needed here, just ensure it's using the destructured `setValue` if that pattern is adopted.

    ```typescript
    // Inside useCharacterSlot
    const handleClearSelection = useCallback(() => {
      setValue(`configs.${index}.characterId`, undefined, { /* RHF options */ });
      setValue(`configs.${index}.llmId`, undefined, { /* RHF options */ });
      // setCharacterDetails(null); // This will be handled by the useEffect watching characterId
    }, [index, setValue]);
    ```
    Actually, `setCharacterDetails(null)` is good to have in `handleClearSelection` for immediate UI feedback if the `useEffect` takes a tick. Or rely purely on the `useEffect`. For robustness and immediate effect, keeping it in `handleClearSelection` and also having the `useEffect` handle it is fine (the `useEffect` will just re-affirm).

### 2.3 Logic Optimization

*   **Reduced Redundancy:** By removing `handleCharacterChange` and `handleLlmChange` and relying on `field.onChange` and `useEffect`, the data flow becomes more unidirectional and aligned with RHF patterns.
*   **Clarity of Side Effects:** Using `useEffect` to manage side effects based on form state changes (like `characterId`) makes the component's behavior more predictable.
*   **Tooltip Logic:** The `tooltipDescription` derived from `characterDetails?.short_description || "Brak danych"` is clear and efficient. No changes needed.
*   **Conditional Rendering:** Logic for displaying character info or placeholders (`Wakat`, `Armchair` icon) is clear.

### 2.4 API Call Management

*   **Location:** The API call `getCharacter` remains within the `useCharacterSlot` hook (specifically, within the `stableFetchCharacterDetails` callback). For this component's scope, this is acceptable. For larger applications, abstracting data fetching into a generic hook (e.g., `useQuery`-like) or a service layer would be beneficial.
*   **Error Handling:** The current error handling uses `toast.error` and `DailogiError`. This is good. Ensure that `setCharacterDetails(null)` or similar state reset occurs on API error to prevent displaying stale data.
*   **Loading State:** Currently, `characterDetails` being `null` can imply a loading state after a character is selected but before details arrive. This could be made more explicit by adding a `isLoading` boolean state if finer-grained control over UI (e.g., showing a spinner in the avatar slot) is desired.
    ```typescript
    // Inside useCharacterSlot
    const [isLoadingDetails, setIsLoadingDetails] = useState(false);

    // In stableFetchCharacterDetails
    // ...
    setIsLoadingDetails(true);
    setCharacterDetails(null);
    try {
      // ... API call ...
      setCharacterDetails(details);
    } catch (error) {
      // ... error handling ...
      setCharacterDetails(null);
    } finally {
      setIsLoadingDetails(false);
    }
    // ...

    // Return isLoadingDetails from the hook to be used in the component.
    ```

### 2.5 Testing Strategy

*   **`useCharacterSlot` Hook Testing (e.g., with `@testing-library/react-hooks` or Vitest equivalent):**
    *   Mock `useFormContext` to provide `watch`, `setValue`, and `control`.
    *   Mock the `getCharacter` API call.
    *   Test initial state.
    *   Test that `characterId` change triggers `fetchCharacterDetails` (mocked) and updates `characterDetails` state.
    *   Test that default LLM is set using `form.setValue` when character details with a default LLM are fetched.
    *   Test that clearing `characterId` (simulating selection of "Odwołaj") clears `llmId` and `characterDetails`.
    *   Test `handleClearSelection` behavior.
    *   Test API error handling: `characterDetails` is reset, and a toast is shown (mock `sonner`).
*   **`CharacterSlot` Component Testing (e.g., with Vitest and `@testing-library/react`):**
    *   Wrap the component in a dummy form using `FormProvider` from RHF.
    *   Mock the `characters` and `llms` props.
    *   Test initial rendering (placeholder state).
    *   Simulate character selection:
        *   Verify `SelectTrigger` shows the placeholder initially.
        *   Open the select, click an option.
        *   Verify the RHF form value for `characterId` is updated.
        *   Verify character name, avatar, and tooltip content are displayed correctly after (mocked) details load.
        *   Verify default LLM is selected if applicable.
    *   Simulate LLM selection:
        *   Verify LLM field is enabled/disabled correctly based on character selection.
        *   Select an LLM and verify RHF form value for `llmId` is updated.
    *   Test clear button:
        *   Click clear, verify form values are reset, and UI returns to placeholder state.
    *   Test tooltip display on hover/focus of the character area.
    *   Test visual distinction for global characters (e.g., presence of `BookOpen` icon, specific class names).
*   **Edge Cases:**
    *   Rapidly changing character selections.
    *   API call taking a long time (test loading state if implemented).
    *   API returning an error.
    *   No characters or LLMs available.
    *   Character with no default LLM.
    *   Character with no avatar.

This refactoring aims to make `CharacterSlot.tsx` more robust, easier to maintain, and more aligned with typical React Hook Form best practices by clarifying data flow and side effect management.
