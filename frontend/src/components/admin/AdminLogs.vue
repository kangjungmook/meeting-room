<template>
  <div class="flex flex-col gap-4">

    <!-- 자동 삭제 안내 -->
    <div class="flex items-center gap-1.5 text-[12px] text-slate-400 font-medium">
      <AppIcon name="clock" :size="12" cls="flex-shrink-0" />
      1개월이 지난 로그는 매일 새벽 3시에 자동 삭제됩니다.
    </div>

    <!-- 카테고리 탭 + 정렬 -->
    <div class="flex gap-2 flex-wrap items-center">
      <button v-for="cat in logCategories" :key="cat.key" @click="logFilter = cat.key"
        :class="['flex items-center gap-2 px-3.5 py-2 rounded-xl text-[13px] font-bold transition-all border',
          logFilter === cat.key
            ? categoryTabClass[cat.color].active + ' border-transparent shadow-sm'
            : 'bg-white text-slate-600 border-slate-200 hover:border-slate-300 hover:bg-slate-50']">
        {{ cat.label }}
        <span :class="['text-[11px] font-bold px-1.5 py-0.5 rounded-full',
          logFilter === cat.key ? categoryTabClass[cat.color].badge + ' bg-white/20' : 'bg-slate-100 text-slate-500']">
          {{ logCategoryCount(cat) }}
        </span>
      </button>
      <button @click="setLogSort('createdAt')" class="ml-auto flex items-center gap-1 px-3 py-2 rounded-xl text-[12px] font-semibold border transition-all"
        :class="logSort.dir === 'desc' ? 'bg-slate-800 text-white border-transparent' : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'">
        <AppIcon name="clock" :size="12" />
        {{ logSort.dir === 'desc' ? '최신순' : '오래된순' }}
      </button>
    </div>

    <!-- 날짜 범위 + 검색 -->
    <div class="flex gap-2 flex-wrap items-center">
      <div class="flex bg-white border border-slate-200 rounded-xl p-0.5 gap-0.5">
        <button v-for="d in dateRangeOptions" :key="d.key" @click="logDateRange = d.key"
          :class="['px-3 py-1.5 rounded-lg text-[12px] font-bold transition-all',
            logDateRange === d.key ? 'bg-slate-800 text-white' : 'text-slate-500 hover:bg-slate-50']">
          {{ d.label }}
        </button>
      </div>
      <input v-model="logSearch" type="text" placeholder="설명 검색..."
        class="flex-1 min-w-[180px] bg-white border border-slate-200 rounded-xl px-4 py-2 text-[13px] text-slate-700 outline-none focus:ring-2 focus:ring-indigo-300 placeholder:text-slate-400" />
      <span class="text-[12px] text-slate-400 font-semibold ml-auto">
        {{ filteredSortedLogs.length }}건 · {{ totalLogPages }}페이지
      </span>
    </div>

    <!-- 로그 테이블 -->
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[960px]">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-200">
              <th class="px-5 py-3 text-left text-[11px] font-bold text-slate-400 uppercase tracking-wider w-36">시간</th>
              <th class="px-5 py-3 text-left text-[11px] font-bold text-slate-400 uppercase tracking-wider w-32">액션</th>
              <th class="px-5 py-3 text-left text-[11px] font-bold text-slate-400 uppercase tracking-wider">설명</th>
              <th class="px-5 py-3 text-left text-[11px] font-bold text-slate-400 uppercase tracking-wider w-36">IP</th>
              <th class="px-5 py-3 text-left text-[11px] font-bold text-slate-400 uppercase tracking-wider w-20">디바이스</th>
              <th class="px-5 py-3 text-left text-[11px] font-bold text-slate-400 uppercase tracking-wider w-28">브라우저</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredSortedLogs.length === 0">
              <td colspan="6" class="px-5 py-16 text-center">
                <div class="flex flex-col items-center gap-2">
                  <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="text-slate-200"><path d="M6 10h28M6 20h20M6 30h12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
                  <p class="text-[14px] font-semibold text-slate-400">로그가 없습니다</p>
                </div>
              </td>
            </tr>
            <template v-for="log in pagedLogs" :key="log.id">
              <tr @click="toggleDetail(log.id)"
                class="border-b border-slate-100 transition-colors cursor-pointer"
                :class="expandedId === log.id ? 'bg-indigo-50/50' : 'hover:bg-slate-50/70'">
                <!-- 시간 -->
                <td class="px-5 py-3.5">
                  <p class="text-[13px] font-semibold text-slate-700 leading-snug">{{ dayjs(log.createdAt).format('YY-MM-DD (ddd)') }}</p>
                  <p class="text-[11px] text-slate-400 mt-0.5">{{ dayjs(log.createdAt).format('HH:mm:ss') }}</p>
                </td>
                <!-- 액션 -->
                <td class="px-5 py-3.5">
                  <span :class="['text-[12px] font-bold px-2.5 py-1 rounded-lg whitespace-nowrap', logBadge(log.actionType)]">
                    {{ actionLabel[log.actionType] || log.actionType }}
                  </span>
                </td>
                <!-- 설명 -->
                <td class="px-5 py-3.5 text-[13px] text-slate-700 font-medium">
                  <div class="flex items-center gap-2">
                    {{ log.description }}
                    <AppIcon name="chevron-down" :size="13"
                      cls="flex-shrink-0 text-slate-400 transition-transform duration-200"
                      :class="expandedId === log.id ? 'rotate-180' : ''" />
                  </div>
                </td>
                <td class="px-5 py-3.5 font-mono text-[12px] text-slate-500">{{ log.ip || '-' }}</td>
                <td class="px-5 py-3.5 text-[12px] text-slate-500">{{ parseDevice(log.userAgent) }}</td>
                <td class="px-5 py-3.5 text-[12px] text-slate-500">{{ parseBrowser(log.userAgent) }}</td>
              </tr>
              <!-- detail 확장 행 -->
              <tr v-if="expandedId === log.id" class="border-b border-slate-100">
                <td colspan="6" class="px-6 py-4 bg-slate-50/80">
                  <!-- 메타 정보 그리드 -->
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-3">
                    <!-- 수행자 -->
                    <div class="bg-white rounded-xl border border-slate-200 px-3 py-2">
                      <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-0.5">수행자</p>
                      <p class="text-[13px] font-semibold text-slate-700">{{ resolveOperator(log.operatorId) }}</p>
                    </div>
                    <!-- HTTP 메서드 + URL -->
                    <div class="bg-white rounded-xl border border-slate-200 px-3 py-2 col-span-1 md:col-span-2">
                      <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-0.5">요청</p>
                      <p class="text-[12px] font-semibold text-slate-700 font-mono flex items-center gap-1.5 truncate">
                        <span v-if="log.httpMethod"
                          :class="['text-[10px] font-black px-1.5 py-0.5 rounded font-sans flex-shrink-0', httpMethodClass(log.httpMethod)]">
                          {{ log.httpMethod }}
                        </span>
                        <span class="truncate">{{ log.requestUrl || '-' }}</span>
                      </p>
                    </div>
                    <!-- 대상 -->
                    <div v-if="log.targetId || log.targetType" class="bg-white rounded-xl border border-slate-200 px-3 py-2">
                      <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-0.5">대상</p>
                      <p class="text-[13px] font-semibold text-slate-700">
                        {{ log.targetType || '' }}<span v-if="log.targetId" class="text-slate-400"> #{{ log.targetId }}</span>
                      </p>
                    </div>
                  </div>

                  <!-- Before / After -->
                  <div v-if="log.beforeData || log.afterData" class="grid grid-cols-2 gap-3 mb-3">
                    <div class="bg-white rounded-xl border border-slate-200 px-3 py-2.5">
                      <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">변경 전</p>
                      <div class="font-mono text-[12px] leading-5 text-slate-700">
                        <AdminLogTree v-if="parseDetail(log.beforeData)" :value="parseDetail(log.beforeData)" />
                        <span v-else class="text-slate-300 italic">없음</span>
                      </div>
                    </div>
                    <div class="bg-white rounded-xl border border-slate-200 px-3 py-2.5">
                      <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">변경 후</p>
                      <div class="font-mono text-[12px] leading-5 text-slate-700">
                        <AdminLogTree v-if="parseDetail(log.afterData)" :value="parseDetail(log.afterData)" />
                        <span v-else class="text-slate-300 italic">없음</span>
                      </div>
                    </div>
                  </div>

                  <!-- detail JSON (before/after 없을 때 폴백) -->
                  <div v-if="log.detail && !log.beforeData && !log.afterData" class="font-mono text-[12px] leading-5 text-slate-700">
                    <AdminLogTree :value="parseDetail(log.detail)" />
                  </div>
                  <p v-else-if="!log.detail && !log.beforeData && !log.afterData" class="text-[12px] text-slate-400 italic">추가 상세 정보 없음</p>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalLogPages > 1" class="flex items-center justify-center gap-2 pt-1">
      <button @click="logPage = 0" :disabled="logPage === 0"
        class="px-3 py-2 rounded-xl text-[12px] font-bold border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-30 transition-all">«</button>
      <button @click="logPage = Math.max(0, logPage - 1)" :disabled="logPage === 0"
        class="px-3 py-2 rounded-xl text-[12px] font-bold border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-30 transition-all">‹</button>
      <div class="flex gap-1">
        <button v-for="p in Math.min(totalLogPages, 7)" :key="p" @click="logPage = p - 1"
          :class="['w-9 h-9 rounded-xl text-[13px] font-bold transition-all',
            logPage === p - 1 ? 'bg-indigo-500 text-white' : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50']">
          {{ p }}
        </button>
      </div>
      <button @click="logPage = Math.min(totalLogPages - 1, logPage + 1)" :disabled="logPage === totalLogPages - 1"
        class="px-3 py-2 rounded-xl text-[12px] font-bold border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-30 transition-all">›</button>
      <button @click="logPage = totalLogPages - 1" :disabled="logPage === totalLogPages - 1"
        class="px-3 py-2 rounded-xl text-[12px] font-bold border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-30 transition-all">»</button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useAdmin } from '../../composables/useAdmin';
import AdminLogTree from './AdminLogTree.vue';
import AppIcon from '../icons/AppIcon.vue';

const {
  logFilter, logSort, logPage, logDateRange, logSearch,
  filteredSortedLogs, pagedLogs, totalLogPages,
  logCategories, actionLabel, categoryTabClass, logCategoryCount, logBadge,
  setLogSort, parseDevice, parseBrowser, parseDetail, dayjs,
  users,
} = useAdmin();

const expandedId = ref(null);

const dateRangeOptions = [
  { key: 'TODAY', label: '오늘' },
  { key: '7D',    label: '7일' },
  { key: '30D',   label: '30일' },
  { key: 'ALL',   label: '전체' },
];

const toggleDetail = (id) => {
  expandedId.value = expandedId.value === id ? null : id;
};

// operatorId → 이름 해석
const resolveOperator = (operatorId) => {
  if (!operatorId) return '-';
  const u = users.value.find(u => u.id === operatorId);
  return u ? `${u.name} (${u.employeeId})` : '(삭제된 관리자)';
};

// HTTP 메서드 색상
const httpMethodClass = (method) => {
  const map = {
    GET:    'bg-emerald-100 text-emerald-700',
    POST:   'bg-blue-100 text-blue-700',
    PUT:    'bg-amber-100 text-amber-700',
    PATCH:  'bg-violet-100 text-violet-700',
    DELETE: 'bg-red-100 text-red-700',
  };
  return map[method] ?? 'bg-slate-100 text-slate-600';
};
</script>
