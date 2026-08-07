import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import TrainingView from '../TrainingView.vue'
import { apiListTrainingRecords, type TrainingRecordVO } from '@/api/training'
import { apiListActions } from '@/api/action'

vi.mock('@/api/training', () => ({
  apiListTrainingRecords: vi.fn(),
  apiCreateTrainingRecord: vi.fn(),
  apiUpdateTrainingRecord: vi.fn(),
  apiGetTrainingRecord: vi.fn(),
  apiDeleteTrainingRecord: vi.fn(),
}))

vi.mock('@/api/action', () => ({
  apiListActions: vi.fn(),
}))

function mockRecord(): TrainingRecordVO {
  return {
    id: 1,
    planId: 1,
    planDayId: null,
    trainingDate: '2026-08-07',
    durationMinutes: 60,
    feel: 'GOOD',
    note: '卧推日',
    planName: '新手全身增肌',
    createdAt: '2026-08-07T10:00:00',
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(apiListTrainingRecords).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: { records: [mockRecord()], total: 1, size: 10, current: 1 },
  })
  vi.mocked(apiListActions).mockResolvedValue({
    code: 200,
    message: 'ok',
    data: {
      records: [
        {
          id: 1,
          categoryId: 1,
          categoryName: '胸部',
          name: '杠铃卧推',
          muscleGroup: 'CHEST',
          difficulty: 'INTERMEDIATE',
          equipment: '杠铃',
          coverImage: null,
          description: null,
        },
      ],
      total: 1,
      size: 100,
      current: 1,
    },
  })
})

describe('TrainingView', () => {
  it('renders training record list with plan name and duration', async () => {
    const wrapper = mount(TrainingView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('2026-08-07')
    expect(wrapper.text()).toContain('新手全身增肌')
    expect(wrapper.text()).toContain('60 分钟')
    expect(wrapper.text()).toContain('状态好')
    expect(wrapper.text()).toContain('卧推日')
  })

  it('opens create dialog when clicking 记录训练', async () => {
    const wrapper = mount(TrainingView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    await wrapper.findAll('button').find((b) => b.text().includes('记录训练'))!.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('记录训练')
    expect(wrapper.text()).toContain('训练组')
    expect(wrapper.text()).toContain('保存')
  })

  it('shows empty state when no records', async () => {
    vi.mocked(apiListTrainingRecords).mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: [], total: 0, size: 10, current: 1 },
    })
    const wrapper = mount(TrainingView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('还没有训练记录')
  })
})
