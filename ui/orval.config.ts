import { defineConfig } from "orval";

export default defineConfig({
  evo: {
    output: {
      mode: "tags-split",
      target: "./src/dailogi-api",
      schemas: "./src/dailogi-api/model",
      //client: "react-query",
      mock: false,
      prettier: true,
      clean: true,
      override: {
        // Force use of interfaces instead of types
        useTypeOverInterfaces: false,
      },
    },
    input: {
      target: "../docs/be-api.json",
    },
  },
});
