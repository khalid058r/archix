# Archix Frontend

Professional React + TypeScript frontend for the Archix document management system.

## Getting Started

### Prerequisites

- Node.js 16+ 
- npm or yarn

### Installation

\`\`\`bash
npm install
\`\`\`

### Environment Setup

Create a `.env` file based on `.env.example`:

\`\`\`
VITE_API_BASE_URL=http://localhost:8080
\`\`\`

### Development

\`\`\`bash
npm run dev
\`\`\`

The app will start at `http://localhost:5173`

### Build

\`\`\`bash
npm run build
\`\`\`

## Project Structure

- `src/api/` - API service layer with Axios HTTP client
- `src/auth/` - Authentication context and protected routes  
- `src/components/` - Reusable UI components and layouts
- `src/pages/` - Page components for each route
- `src/hooks/` - Custom React hooks
- `src/types/` - TypeScript interfaces and types
- `src/routing/` - Route configuration
- `src/styles/` - Global styles and Tailwind CSS

## Features

- ✅ Authentication (Login/Register)
- ✅ JWT token management with auto-refresh on 401
- ✅ Protected routes
- ✅ User management with CRUD operations
- ✅ Department and Organization management
- ✅ Namespace tree view and document search
- ✅ Permission management
- ✅ Zod validation for forms
- ✅ Responsive design with Tailwind CSS
- ✅ Toast notifications (to be implemented)
- ✅ Error handling

## API Integration

All API calls go through the HTTP client at `src/api/http-client.ts` which:
- Automatically injects JWT token in Authorization header
- Handles 401 responses by redirecting to login
- Provides consistent error handling

## Tech Stack

- React 18+
- TypeScript
- React Router v6
- Axios
- Zod (validation)
- Tailwind CSS
- Lucide Icons
- Vite

## Future Improvements

- [ ] Implement React Query for caching
- [ ] Add toast notifications
- [ ] Implement server-side pagination
- [ ] Add unit tests with Vitest
- [ ] Implement i18n with react-i18next
- [ ] Add drag & drop for namespace tree
- [ ] File upload functionality
- [ ] Debounced search across all lists
- [ ] Dark mode support
