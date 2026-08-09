import React, { useState, useEffect } from 'react';
import { exploreSkills, getSkillCategories, searchMarketplaceUsers } from '../../../lib/apiClient';
import { ExploreSkill, SkillCategory, PublicUserProfile } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { SkillLevelBadge } from '../../../components/common/SkillLevelBadge';
import { ExchangeRequestModal } from '../../../components/marketplace/ExchangeRequestModal';
import { Search, Compass, Users, BookOpen, X, ArrowLeftRight, CheckCircle2 } from 'lucide-react';

interface FlatCategory {
  id: string;
  name: string;
}

function flattenCategoryTree(tree: SkillCategory[], prefix = ''): FlatCategory[] {
  let result: FlatCategory[] = [];
  for (const cat of tree) {
    const label = prefix ? `${prefix} › ${cat.name}` : cat.name;
    result.push({ id: cat.id, name: label });
    if (cat.children && cat.children.length > 0) {
      result = result.concat(flattenCategoryTree(cat.children, label));
    }
  }
  return result;
}

export const ExploreSkillsPage: React.FC = () => {
  const [skills, setSkills] = useState<ExploreSkill[]>([]);
  const [categories, setCategories] = useState<FlatCategory[]>([]);
  const [search, setSearch] = useState('');
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const [loading, setLoading] = useState(true);

  const [selectedSkill, setSelectedSkill] = useState<ExploreSkill | null>(null);
  const [peerUsers, setPeerUsers] = useState<PublicUserProfile[]>([]);
  const [loadingPeers, setLoadingPeers] = useState(false);

  const [targetPeer, setTargetPeer] = useState<PublicUserProfile | null>(null);
  const [isExchangeModalOpen, setIsExchangeModalOpen] = useState(false);
  const [requestSentUserIds, setRequestSentUserIds] = useState<Set<string>>(new Set());

  useEffect(() => {
    getSkillCategories()
      .then((tree) => setCategories(flattenCategoryTree(Array.isArray(tree) ? tree : [])))
      .catch(console.error);
  }, []);

  useEffect(() => {
    setLoading(true);
    exploreSkills(search, selectedCategoryId).then(setSkills).catch(console.error).finally(() => setLoading(false));
  }, [search, selectedCategoryId]);

  const handleOpenPeers = async (skill: ExploreSkill) => {
    setSelectedSkill(skill);
    setLoadingPeers(true);
    try {
      const users = await searchMarketplaceUsers(undefined, skill.id);
      setPeerUsers(users);
    } catch (err) { console.error(err); }
    finally { setLoadingPeers(false); }
  };

  return (
    <div className="space-y-7">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
          <Compass className="w-6 h-6 text-teal-500" />
          Skill Catalog
        </h1>
        <p className="text-sm text-slateText-500 mt-1">
          Explore your institution's skill tree and find peers who can teach or learn.
        </p>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slateText-400 pointer-events-none" />
          <input
            type="text"
            placeholder="Search skills (e.g. Java, Figma, SQL)…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-white border border-surface-200 rounded-input pl-10 pr-4 py-2.5 text-sm text-slateText-900 placeholder-slateText-400 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500"
          />
        </div>
        <select
          value={selectedCategoryId}
          onChange={(e) => setSelectedCategoryId(e.target.value)}
          className="bg-white border border-surface-200 rounded-input px-3.5 py-2.5 text-sm text-slateText-900 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500 sm:w-56"
        >
          <option value="">All Categories</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>{cat.name}</option>
          ))}
        </select>
      </div>

      {/* Skill Grid */}
      {loading ? (
        <div className="py-12 text-center text-sm text-slateText-400">Loading catalog…</div>
      ) : skills.length === 0 ? (
        <Card className="p-10 text-center">
          <p className="text-sm text-slateText-500">No skills match your search. Try a different term or category.</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {skills.map((skill) => (
            <Card key={skill.id} className="p-5 flex flex-col justify-between group hover:shadow-md transition-shadow">
              <div>
                <div className="flex items-start justify-between mb-2">
                  <h3 className="font-semibold text-slateText-900 text-sm group-hover:text-teal-600 transition-colors">
                    {skill.name}
                  </h3>
                  <span className="text-[10px] bg-surface-100 text-slateText-500 border border-surface-200 px-2 py-0.5 rounded font-medium shrink-0 ml-2">
                    {skill.categoryName}
                  </span>
                </div>
                {skill.description && (
                  <p className="text-xs text-slateText-400 line-clamp-2 mb-3">{skill.description}</p>
                )}
              </div>
              <div className="pt-3 border-t border-surface-100 flex items-center justify-between">
                <div className="flex items-center gap-3 text-xs text-slateText-400">
                  <span className="flex items-center gap-1 text-teal-600 font-medium">
                    <Users className="w-3 h-3" /> {skill.teacherCount} teach
                  </span>
                  <span className="flex items-center gap-1 text-aiBlue font-medium">
                    <BookOpen className="w-3 h-3" /> {skill.learnerCount} learn
                  </span>
                </div>
                <button
                  onClick={() => handleOpenPeers(skill)}
                  className="text-xs text-teal-600 hover:text-teal-700 font-semibold"
                >
                  View Peers →
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* ─── Peer Discovery Modal ─── */}
      {selectedSkill && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white border border-surface-200 rounded-panel w-full max-w-2xl flex flex-col max-h-[85vh] shadow-xl">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-surface-200 flex items-center justify-between shrink-0">
              <div>
                <h2 className="text-base font-semibold text-slateText-900 flex items-center gap-2">
                  <Users className="w-4 h-4 text-teal-500" />
                  Peers teaching <span className="text-teal-600">{selectedSkill.name}</span>
                </h2>
                <p className="text-xs text-slateText-400 mt-0.5">{selectedSkill.categoryName}</p>
              </div>
              <button onClick={() => setSelectedSkill(null)} className="text-slateText-400 hover:text-slateText-700 transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 overflow-y-auto space-y-3 flex-1">
              {loadingPeers ? (
                <div className="py-8 text-center text-sm text-slateText-400">Finding qualified peers…</div>
              ) : peerUsers.length === 0 ? (
                <div className="py-8 text-center">
                  <Users className="w-8 h-8 text-slateText-300 mx-auto mb-2" />
                  <p className="text-sm text-slateText-500">
                    No peers have listed "{selectedSkill.name}" as teachable yet.
                  </p>
                </div>
              ) : (
                peerUsers.map((peer) => {
                  const teachableSkill = peer.skillsTeaching.find((s) => s.skillId === selectedSkill.id);
                  const isSent = requestSentUserIds.has(peer.user.id);
                  return (
                    <div
                      key={peer.user.id}
                      className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 p-4 bg-surface-50 border border-surface-200 rounded-card hover:border-teal-200 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-teal-500 text-white font-bold flex items-center justify-center text-sm shrink-0">
                          {peer.user.firstName[0]}{peer.user.lastName[0]}
                        </div>
                        <div>
                          <p className="font-semibold text-slateText-900 text-sm">
                            {peer.user.firstName} {peer.user.lastName}
                          </p>
                          <p className="text-xs text-slateText-400">{peer.user.email}</p>
                          {teachableSkill && (
                            <div className="flex items-center gap-2 mt-1">
                              <SkillLevelBadge level={teachableSkill.level} />
                              {teachableSkill.yearsExperience && (
                                <span className="text-[11px] text-slateText-400">
                                  {teachableSkill.yearsExperience} yrs exp
                                </span>
                              )}
                            </div>
                          )}
                        </div>
                      </div>
                      <Button
                        size="sm"
                        variant={isSent ? 'secondary' : 'primary'}
                        disabled={isSent}
                        onClick={() => { setTargetPeer(peer); setIsExchangeModalOpen(true); }}
                      >
                        {isSent ? (
                          <><CheckCircle2 className="w-3.5 h-3.5 mr-1.5 text-green-500" /> Sent</>
                        ) : (
                          <><ArrowLeftRight className="w-3.5 h-3.5 mr-1.5" /> Request Exchange</>
                        )}
                      </Button>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      )}

      <ExchangeRequestModal
        isOpen={isExchangeModalOpen}
        onClose={() => setIsExchangeModalOpen(false)}
        targetUser={targetPeer}
        onSuccess={() => {
          if (targetPeer) setRequestSentUserIds((prev) => new Set(prev).add(targetPeer.user.id));
        }}
      />
    </div>
  );
};
