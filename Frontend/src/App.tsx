import { Provider } from 'react-redux';
import { Toaster } from 'react-hot-toast';
import { store } from './store';
import { AppRouter } from './routes';
import './styles/globals.css';

import { AuthInitializer } from './components/auth/AuthInitializer';

function App() {
  return (
    <Provider store={store}>
      <AuthInitializer>
        <AppRouter />
      </AuthInitializer>
      <Toaster position="top-right" toastOptions={{ duration: 4000 }} />
    </Provider>
  );
}

export default App;
