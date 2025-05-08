# Stage 1: Build the Astro application
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package.json and lock file
COPY package.json package-lock.json* ./
# If you use pnpm or yarn, adjust to pnpm-lock.yaml or yarn.lock and use pnpm install or yarn install

# Install dependencies
RUN npm ci

# Copy the rest of the application source code
COPY . .

# Build the application
# Ensure any required build-time environment variables are passed here using ARGs if needed
RUN npm run build

# Stage 2: Create the runtime image
FROM node:22-alpine

WORKDIR /app

# Create a non-root user and group
RUN addgroup -S nodeusergroup && adduser -S nodeuser -G nodeusergroup

# Copy built artifacts and necessary files from the builder stage
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package.json .
COPY --from=builder /app/package-lock.json* .
# If using pnpm/yarn, copy respective files and use pnpm/yarn for install

# Install only production dependencies for the runtime
# Astro's standalone adapter might bundle many things, but this ensures any peer deps or dynamically loaded server deps are present.
RUN npm ci --omit=dev

# Set ownership of app files
RUN chown -R nodeuser:nodeusergroup /app

# Switch to the non-root user
USER nodeuser

# Expose the application port (default 3000, can be overridden by PORT env var)
EXPOSE 3000

# Environment variables for the Node.js server
# HOST=0.0.0.0 makes the server accessible from outside the container
ENV PORT=3000
ENV HOST=0.0.0.0

# Healthcheck (simple TCP check, customize if a specific health endpoint is available)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD node -e "const http = require('http'); http.get({ host: 'localhost', port: process.env.PORT || 3000, path: '/' }, (res) => process.exit(res.statusCode === 200 || res.statusCode === 304 || res.statusCode === 404 ? 0 : 1)).on('error', () => process.exit(1));"

# Command to run the Astro server
# The entry point is typically dist/server/entry.mjs for Astro's node adapter
CMD ["node", "dist/server/entry.mjs"] 