import React, { useEffect, useState } from 'react';
import { getWallet, getCreditTransactions } from '../../../lib/apiClient';
import { CreditWallet, CreditTransaction } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Coins, TrendingUp, TrendingDown, RefreshCw } from 'lucide-react';
import { Button } from '../../../components/ui/Button';

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', { dateStyle: 'medium' });
}

const TX_COLORS: Record<string, string> = {
  EARN:    'bg-teal-50 text-teal-700 border-teal-200',
  SPEND:   'bg-violet-50 text-violet-700 border-violet-200',
  INITIAL: 'bg-surface-100 text-slateText-500 border-surface-200',
};

export const CreditsPage: React.FC = () => {
  const [wallet, setWallet] = useState<CreditWallet | null>(null);
  const [transactions, setTransactions] = useState<CreditTransaction[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const [w, t] = await Promise.all([getWallet(), getCreditTransactions()]);
      setWallet(w);
      setTransactions(Array.isArray(t) ? t : []);
    } catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const earned  = transactions.filter((t) => t.type === 'EARN').reduce((s, t) => s + t.amount, 0);
  const spent   = transactions.filter((t) => t.type === 'SPEND').reduce((s, t) => s + Math.abs(t.amount), 0);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
            <Coins className="w-6 h-6 text-teal-500" />
            Skill Credits
          </h1>
          <p className="text-sm text-slateText-500 mt-0.5">Your credit ledger — earn by teaching, spend by learning</p>
        </div>
        <Button variant="outline" size="sm" onClick={load}>
          <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Wallet cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-5 col-span-1 bg-gradient-to-br from-teal-500 to-teal-600 text-white border-0">
          <p className="text-xs font-medium text-teal-100 mb-1">Current Balance</p>
          <div className="text-4xl font-bold">
            {loading ? '—' : wallet?.balance.toFixed(2) ?? '—'}
          </div>
          <p className="text-sm text-teal-200 mt-1">credits</p>
        </Card>

        <Card className="p-5">
          <div className="flex items-center gap-2 mb-2">
            <TrendingUp className="w-4 h-4 text-teal-500" />
            <p className="text-xs font-medium text-slateText-600">Total Earned</p>
          </div>
          <p className="text-2xl font-bold text-slateText-900">
            +{loading ? '—' : earned.toFixed(2)}
          </p>
          <p className="text-xs text-slateText-400 mt-0.5">from teaching sessions</p>
        </Card>

        <Card className="p-5">
          <div className="flex items-center gap-2 mb-2">
            <TrendingDown className="w-4 h-4 text-violet-500" />
            <p className="text-xs font-medium text-slateText-600">Total Spent</p>
          </div>
          <p className="text-2xl font-bold text-slateText-900">
            -{loading ? '—' : spent.toFixed(2)}
          </p>
          <p className="text-xs text-slateText-400 mt-0.5">from learning sessions</p>
        </Card>
      </div>

      {/* Transaction history */}
      <div>
        <h2 className="text-sm font-semibold text-slateText-900 mb-3">Transaction History</h2>

        {loading ? (
          <div className="space-y-2">
            {[1, 2, 3, 4].map((i) => <div key={i} className="h-14 bg-surface-100 rounded-btn animate-pulse" />)}
          </div>
        ) : transactions.length === 0 ? (
          <Card className="p-8 text-center">
            <Coins className="w-8 h-8 text-surface-300 mx-auto mb-2" />
            <p className="text-sm text-slateText-500">No transactions yet. Complete sessions to earn credits!</p>
          </Card>
        ) : (
          <Card className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-surface-200 bg-surface-50">
                    <th className="text-left text-[11px] font-semibold text-slateText-500 uppercase tracking-wider px-4 py-2.5">Type</th>
                    <th className="text-left text-[11px] font-semibold text-slateText-500 uppercase tracking-wider px-4 py-2.5">Amount</th>
                    <th className="text-left text-[11px] font-semibold text-slateText-500 uppercase tracking-wider px-4 py-2.5 hidden sm:table-cell">Balance After</th>
                    <th className="text-left text-[11px] font-semibold text-slateText-500 uppercase tracking-wider px-4 py-2.5 hidden md:table-cell">Reference</th>
                    <th className="text-left text-[11px] font-semibold text-slateText-500 uppercase tracking-wider px-4 py-2.5">Date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-100">
                  {transactions.map((tx) => (
                    <tr key={tx.id} className="hover:bg-surface-50 transition-colors">
                      <td className="px-4 py-3">
                        <span className={`text-[11px] font-semibold border px-2 py-0.5 rounded-full ${TX_COLORS[tx.type] ?? TX_COLORS.INITIAL}`}>
                          {tx.type === 'EARN' ? '+EARN' : tx.type === 'SPEND' ? '−SPEND' : tx.type}
                        </span>
                      </td>
                      <td className={`px-4 py-3 font-bold ${tx.amount >= 0 ? 'text-teal-600' : 'text-violet-600'}`}>
                        {tx.amount >= 0 ? '+' : ''}{tx.amount.toFixed(2)}
                      </td>
                      <td className="px-4 py-3 text-slateText-600 hidden sm:table-cell">
                        {tx.balanceAfter.toFixed(2)}
                      </td>
                      <td className="px-4 py-3 text-slateText-400 text-xs hidden md:table-cell truncate max-w-[140px]">
                        {tx.referenceType} · {tx.referenceId?.slice(0, 8)}…
                      </td>
                      <td className="px-4 py-3 text-slateText-500 text-xs">{formatDate(tx.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};
