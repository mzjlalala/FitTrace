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
  MUSCLE_LABEL: { CHEST: '胸部', BACK: '背部', LEGS: '腿部', SHOULDERS: '肩部', BICEPS: '二头', TRICEPS: '三头', CORE: '核心', CARDIO: '有氧' },
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
        {
          id: 2,
          categoryId: 2,
          categoryName: '背部',
          name: '高位下拉',
          muscleGroup: 'BACK',
          difficulty: 'INTERMEDIATE',
          equipment: '绳索',
          coverImage: null,
          description: null,
        },
        {
          id: 3,
          categoryId: 3,
          categoryName: '手臂',
          name: '哑铃弯举',
          muscleGroup: 'BICEPS',
          difficulty: 'BEGINNER',
          equipment: '哑铃',
          coverImage: null,
          description: null,
        },
      ],
      total: 3,
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

    // 默认按当月范围查询：起始为当月 1 号，结束为今天
    expect(vi.mocked(apiListTrainingRecords)).toHaveBeenCalledWith(
      1,
      10,
      expect.stringMatching(/^\d{4}-\d{2}-01$/),
      expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
    )
  })

  it('opens create dialog when clicking 记录训练', async () => {
    const wrapper = mount(TrainingView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    await wrapper.findAll('button').find((b) => b.text().includes('记录训练'))!.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('记录训练')
    expect(wrapper.text()).toContain('训练组')
    expect(wrapper.text()).toContain('保存')

    wrapper.unmount()
  })

  it('filters action options by muscle group in create dialog', async () => {
    const wrapper = mount(TrainingView, { global: { plugins: [ElementPlus] }, attachTo: document.body })
    await flushPromises()
    await wrapper.findAll('button').find((b) => b.text().includes('记录训练'))!.trigger('click')
    await flushPromises()

    // 弹窗内的肌群筛选下拉（弹窗内第 2 个 el-select：感受、肌群筛选、动作…）
    await wrapper.findAll('.el-select')[1]!.find('.el-select__wrapper').trigger('click')
    await flushPromises()
    const backOption = Array.from(document.querySelectorAll('.el-select-dropdown__item')).find(
      (el) => el.textContent?.trim() === '背部',
    ) as HTMLElement
    expect(backOption).toBeTruthy()
    backOption.click()
    await flushPromises()

    // 选择「背部」肌群后，动作下拉只展示背部动作 + 已选动作（第一行默认选中了第一个动作）
    await wrapper.findAll('.el-select')[2]!.find('.el-select__wrapper').trigger('click')
    await flushPromises()
    // 只统计当前展开（可见）的下拉，避免匹配到已收起/残留的下拉
    const options = Array.from(
      document.querySelectorAll('.el-select-dropdown:not([style*="display: none"]) .el-select-dropdown__item'),
    ).map((el) => el.textContent?.trim())
    expect(options).toContain('高位下拉')
    // 筛选生效：其他肌群的动作不在选项中
    expect(options).not.toContain('哑铃弯举')
    // 已选动作保留，切换肌群不丢失当前行选择
    expect(options).toContain('杠铃卧推')

    wrapper.unmount()
    document.body.innerHTML = ''
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
