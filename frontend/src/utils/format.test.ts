import { describe, expect, test } from 'vitest'
import { classNames, formatCurrency } from './format'

describe('format utilities', () => {
  test('formatCurrency handles null and number', () => {
    expect(formatCurrency(null)).toBe('N/A')
    expect(formatCurrency(123.45)).toContain('123')
  })

  test('classNames joins truthy values', () => {
    expect(classNames('a', '', null, 'b', false, 'c')).toBe('a b c')
  })
})
