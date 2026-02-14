import React, { useState } from 'react';
import { 
    ChevronUp, 
    ChevronDown, 
    ChevronLeft, 
    ChevronRight,
    ChevronsLeft,
    ChevronsRight,
    Search,
    Download,
    Filter
} from 'lucide-react';
import { cn } from '../../utils/cn';
import { Button } from '../ui/Button/Button';

export interface Column<T> {
    key: string;
    header: string;
    sortable?: boolean;
    filterable?: boolean;
    width?: string;
    render?: (item: T) => React.ReactNode;
}

interface DataTableProps<T> {
    data: T[];
    columns: Column<T>[];
    loading?: boolean;
    searchable?: boolean;
    searchPlaceholder?: string;
    exportable?: boolean;
    onExport?: () => void;
    pageSize?: number;
    emptyMessage?: string;
    onRowClick?: (item: T) => void;
    selectedRows?: T[];
    onSelectionChange?: (items: T[]) => void;
    getRowId?: (item: T) => string | number;
}

export function DataTable<T extends object>({
    data,
    columns,
    loading = false,
    searchable = true,
    searchPlaceholder = 'Rechercher...',
    exportable = false,
    onExport,
    pageSize = 10,
    emptyMessage = 'Aucune donnée trouvée',
    onRowClick,
    selectedRows = [],
    onSelectionChange,
    getRowId = (item) => (item as { id?: string | number }).id ?? 0,
}: DataTableProps<T>) {
    const [searchTerm, setSearchTerm] = useState('');
    const [sortKey, setSortKey] = useState<string | null>(null);
    const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
    const [currentPage, setCurrentPage] = useState(1);
    const [showFilters, setShowFilters] = useState(false);

    // Helper to safely access object properties
    const getItemValue = (item: T, key: string): unknown => {
        return (item as Record<string, unknown>)[key];
    };

    // Filter data based on search term
    const filteredData = data.filter(item => {
        if (!searchTerm) return true;
        return columns.some(col => {
            const value = getItemValue(item, col.key);
            if (value == null) return false;
            return String(value).toLowerCase().includes(searchTerm.toLowerCase());
        });
    });

    // Sort data
    const sortedData = [...filteredData].sort((a, b) => {
        if (!sortKey) return 0;
        const aVal = getItemValue(a, sortKey);
        const bVal = getItemValue(b, sortKey);
        if (aVal == null) return 1;
        if (bVal == null) return -1;
        const comparison = String(aVal).localeCompare(String(bVal));
        return sortDir === 'asc' ? comparison : -comparison;
    });

    // Paginate data
    const totalPages = Math.ceil(sortedData.length / pageSize);
    const paginatedData = sortedData.slice(
        (currentPage - 1) * pageSize,
        currentPage * pageSize
    );

    const handleSort = (key: string) => {
        if (sortKey === key) {
            setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
        } else {
            setSortKey(key);
            setSortDir('asc');
        }
    };

    const handleSelectAll = () => {
        if (!onSelectionChange) return;
        if (selectedRows.length === paginatedData.length) {
            onSelectionChange([]);
        } else {
            onSelectionChange(paginatedData);
        }
    };

    const handleSelectRow = (item: T) => {
        if (!onSelectionChange) return;
        const id = getRowId(item);
        const isSelected = selectedRows.some(row => getRowId(row) === id);
        if (isSelected) {
            onSelectionChange(selectedRows.filter(row => getRowId(row) !== id));
        } else {
            onSelectionChange([...selectedRows, item]);
        }
    };

    return (
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            {/* Toolbar */}
            {(searchable || exportable) && (
                <div className="p-4 border-b border-gray-200 flex flex-wrap items-center gap-3">
                    {searchable && (
                        <div className="relative flex-1 min-w-[200px] max-w-md">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                            <input
                                type="text"
                                placeholder={searchPlaceholder}
                                value={searchTerm}
                                onChange={(e) => {
                                    setSearchTerm(e.target.value);
                                    setCurrentPage(1);
                                }}
                                className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all"
                            />
                        </div>
                    )}
                    
                    <div className="flex items-center gap-2 ml-auto">
                        <Button 
                            variant="outline" 
                            size="sm"
                            onClick={() => setShowFilters(!showFilters)}
                        >
                            <Filter size={16} className="mr-2" />
                            Filtres
                        </Button>
                        
                        {exportable && onExport && (
                            <Button variant="outline" size="sm" onClick={onExport}>
                                <Download size={16} className="mr-2" />
                                Exporter
                            </Button>
                        )}
                    </div>
                </div>
            )}

            {/* Table */}
            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                            {onSelectionChange && (
                                <th className="w-12 px-4 py-3">
                                    <input
                                        type="checkbox"
                                        checked={selectedRows.length === paginatedData.length && paginatedData.length > 0}
                                        onChange={handleSelectAll}
                                        className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                    />
                                </th>
                            )}
                            {columns.map((col) => (
                                <th
                                    key={col.key}
                                    className={cn(
                                        'px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider',
                                        col.sortable && 'cursor-pointer hover:bg-gray-100 transition-colors'
                                    )}
                                    style={{ width: col.width }}
                                    onClick={() => col.sortable && handleSort(col.key)}
                                >
                                    <div className="flex items-center gap-2">
                                        {col.header}
                                        {col.sortable && sortKey === col.key && (
                                            sortDir === 'asc' ? <ChevronUp size={14} /> : <ChevronDown size={14} />
                                        )}
                                    </div>
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {loading ? (
                            <tr>
                                <td colSpan={columns.length + (onSelectionChange ? 1 : 0)} className="px-4 py-12 text-center">
                                    <div className="flex items-center justify-center gap-2 text-gray-500">
                                        <div className="w-5 h-5 border-2 border-primary/30 border-t-primary rounded-full animate-spin" />
                                        Chargement...
                                    </div>
                                </td>
                            </tr>
                        ) : paginatedData.length === 0 ? (
                            <tr>
                                <td colSpan={columns.length + (onSelectionChange ? 1 : 0)} className="px-4 py-12 text-center text-gray-500">
                                    {emptyMessage}
                                </td>
                            </tr>
                        ) : (
                            paginatedData.map((item) => {
                                const id = getRowId(item);
                                const isSelected = selectedRows.some(row => getRowId(row) === id);
                                
                                return (
                                    <tr
                                        key={id}
                                        className={cn(
                                            'hover:bg-gray-50 transition-colors',
                                            onRowClick && 'cursor-pointer',
                                            isSelected && 'bg-primary/5'
                                        )}
                                        onClick={() => onRowClick?.(item)}
                                    >
                                        {onSelectionChange && (
                                            <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                                                <input
                                                    type="checkbox"
                                                    checked={isSelected}
                                                    onChange={() => handleSelectRow(item)}
                                                    className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                                />
                                            </td>
                                        )}
                                        {columns.map((col) => (
                                            <td key={col.key} className="px-4 py-3 text-sm text-gray-700">
                                                {col.render ? col.render(item) : String(getItemValue(item, col.key) ?? '-')}
                                            </td>
                                        ))}
                                    </tr>
                                );
                            })
                        )}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="px-4 py-3 border-t border-gray-200 flex items-center justify-between">
                    <div className="text-sm text-gray-500">
                        Affichage {((currentPage - 1) * pageSize) + 1} - {Math.min(currentPage * pageSize, sortedData.length)} sur {sortedData.length}
                    </div>
                    <div className="flex items-center gap-1">
                        <button
                            onClick={() => setCurrentPage(1)}
                            disabled={currentPage === 1}
                            className="p-2 rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <ChevronsLeft size={18} />
                        </button>
                        <button
                            onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                            disabled={currentPage === 1}
                            className="p-2 rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <ChevronLeft size={18} />
                        </button>
                        
                        <div className="flex items-center gap-1 px-2">
                            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                let page: number;
                                if (totalPages <= 5) {
                                    page = i + 1;
                                } else if (currentPage <= 3) {
                                    page = i + 1;
                                } else if (currentPage >= totalPages - 2) {
                                    page = totalPages - 4 + i;
                                } else {
                                    page = currentPage - 2 + i;
                                }
                                
                                return (
                                    <button
                                        key={page}
                                        onClick={() => setCurrentPage(page)}
                                        className={cn(
                                            'w-8 h-8 rounded text-sm font-medium transition-colors',
                                            currentPage === page
                                                ? 'bg-primary text-white'
                                                : 'hover:bg-gray-100 text-gray-700'
                                        )}
                                    >
                                        {page}
                                    </button>
                                );
                            })}
                        </div>

                        <button
                            onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                            disabled={currentPage === totalPages}
                            className="p-2 rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <ChevronRight size={18} />
                        </button>
                        <button
                            onClick={() => setCurrentPage(totalPages)}
                            disabled={currentPage === totalPages}
                            className="p-2 rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <ChevronsRight size={18} />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default DataTable;
