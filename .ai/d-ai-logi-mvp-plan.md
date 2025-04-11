# Założenia projektu MVP d-AI-logi

### Główny problem
Tworzenie interakcji między różnymi osobowościami AI może być skomplikowane i często wymaga wiedzy technicznej oraz osobnej konfiguracji wielu modeli. Jednocześnie, fani różnych uniwersów nie mają prostego narzędzia do tworzenia hipotetycznych dialogów między ulubionymi postaciami.

### Najmniejszy zestaw funkcjonalności
- Biblioteka gotowych osobowości AI do wyboru (zarówno predefiniowane archetypy, jak i popularne postacie fikcyjne).
- Możliwość tworzenia własnych, prostych osobowości AI poprzez krótki opis charakteru.
- Możliwość wyboru i zmiany modelu LLM (silnika AI), który będzie napędzał każdą z osobowości (zarówno gotowych, jak i stworzonych przez użytkownika).
- Interfejs pozwalający na zestawienie 2-3 różnych osobowości do interakcji.
- System konwersacji, w którym dialogi między wybranymi postaciami generują się automatycznie tura po turze, pozwalający na ich obserwację.
- Zapisywanie i odczytywanie historii dialogów
- Możliwość zadania tematu/pytania rozpoczynającego dialog oraz opisania okoliczności/scenerii, w której odbywa się rozmowa
- Podstawowy system kont użytkowników (rejestracja/logowanie) umożliwiający zapisywanie utworzonych scen i dostęp do prywatnej galerii postaci.
- Możliwość dodawania, edytowania i usuwania własnych stworzonych osobowości AI w prywatnej galerii użytkownika.

### Co NIE wchodzi w zakres MVP
- Parametryzowanie postaci (np. suwaki intensywności emocji, stopnia elokwencji)
- Zaawansowane narzędzia do tworzenia własnych osobowości (np. trenowanie na korpusach tekstów, długie definicje).
- Możliwość konfigurowania zaawansowanych parametrów modeli LLM przez użytkownika (np. temperatura, top_p, max tokens).
- Interakcje więcej niż 3 postaci jednocześnie
- System ocen i rekomendacji interesujących zestawień postaci
- Dzielenie się postaciami pomiędzy użytkownikami
- Udostępnianie rozegranych scen innym użytkownikom
- Eksport dialogów do różnych formatów (np. scenariusze, pliki tekstowe).
- Wsparcie dla modeli lokalnych (np. własne instancje LLaMA) i niestandardowych endpointów AI
- Brak możliwości interwencji użytkownika w trakcie automatycznie generowanego dialogu

### Kryteria sukcesu
- Średni czas spędzony przez użytkownika w aplikacji podczas jednej sesji wynosi minimum 5 minut.
- 20% zarejestrowanych użytkowników tworzy przynajmniej jedną własną osobowość AI
- 50% zarejestrowanych użytkowników wraca do aplikacji w ciągu tygodnia od pierwszego użycia

