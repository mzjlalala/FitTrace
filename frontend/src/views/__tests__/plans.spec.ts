import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import PlanDetailView from '../PlanDetailView.vue'
import { apiGetMyPlans, apiGetPlan, type PlanDetail } from '@/api/plan'

vi.mock('@/api/plan', () => ({
  apiGetPlan: vi.fn(),
  apiGetMyPlans: vi.fn(),
  apiStartPlan: vi.fn(),
  apiListPlans: vi.fn(),
  apiRecommendPlans: vi.fn(),
  apiUpdateUserPlan: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '3' } }),
  useRouter: () => ({ back: vi.fn(), push: vi.fn() }),
}))

function mockPlanDetail(): PlanDetail {
  return {
    id: 3,
    name: '力量进阶',
    goal: 'STRENGTH',
    level: 'INTERMEDIATE',
    durationWeeks: 6,
    frequencyPerWeek: 4,
    description: '6 周力量提升计划',
    coverImage: null,
    weeks: [
      {
        id: 10,
        weekNo: 1,
        days: [
          {
            id: 11,
            dayNo: 1,
            restFlag: false,
            title: '推日',
            actions: [
              {
                id: 100,
                sort: 1,
                sets: 5,
                reps: 5,
                weightMode: 'FIXED',
                restSeconds: 120,
                action: {
                  id: 1,
                  name: '杠铃卧推',
                  muscleGroup: 'CHEST',
                  difficulty: 'INTERMEDIATE',
                  equipment: '杠铃',
                },
              },
            ],
          },
          { id: 12, dayNo: 2, restFlag: true, title: null, actions: [] },
        ],
      },
    ],
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(apiGetPlan).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: mockPlanDetail(),
  })
  vi.mocked(apiGetMyPlans).mockResolvedValue({ code: 200, message: 'ok', data: [] })
})

describe('PlanDetailView', () => {
  it('renders plan info, day actions and start button', async () => {
    const wrapper = mount(PlanDetailView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('力量进阶')
    expect(wrapper.text()).toContain('Day 1')
    expect(wrapper.text()).toContain('杠铃卧推')
    expect(wrapper.text()).toContain('5 × 5')
    expect(wrapper.text()).toContain('开始计划')
  })

  it('shows active alert and hides start button when already subscribed', async () => {
    vi.mocked(apiGetMyPlans).mockResolvedValue({
      code: 200,
      message: 'ok',
      data: [
        {
          id: 9,
          planId: 3,
          planName: '力量进阶',
          planGoal: 'STRENGTH',
          planLevel: 'INTERMEDIATE',
          startDate: '2026-08-07',
          status: 'ACTIVE',
        },
      ],
    })
    const wrapper = mount(PlanDetailView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('你正在进行该计划')
    expect(wrapper.text()).not.toContain('开始计划')
  })
})
