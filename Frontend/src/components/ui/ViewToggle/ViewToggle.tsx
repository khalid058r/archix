import { LayoutGrid, List } from 'lucide-react';
import { Button } from '../Button/Button';

interface ViewToggleProps {
    viewMode: 'list' | 'grid';
    onChange: (mode: 'list' | 'grid') => void;
}

export const ViewToggle = ({ viewMode, onChange }: ViewToggleProps) => {
    return (
        <div className="flex items-center bg-gray-100 p-1 rounded-lg border border-gray-200">
            <button
                type="button"
                className={`p-2 rounded-md transition-all ${viewMode === 'list'
                        ? 'bg-white shadow-sm text-primary'
                        : 'text-gray-500 hover:text-gray-700 hover:bg-gray-200/50'
                    }`}
                onClick={() => {
                    console.log('Switch to LIST');
                    onChange('list');
                }}
                title="Vue Liste"
            >
                <List size={18} />
            </button>
            <button
                type="button"
                className={`p-2 rounded-md transition-all ${viewMode === 'grid'
                        ? 'bg-white shadow-sm text-primary'
                        : 'text-gray-500 hover:text-gray-700 hover:bg-gray-200/50'
                    }`}
                onClick={() => {
                    console.log('Switch to GRID');
                    onChange('grid');
                }}
                title="Vue Grille"
            >
                <LayoutGrid size={18} />
            </button>
        </div>
    );
};
