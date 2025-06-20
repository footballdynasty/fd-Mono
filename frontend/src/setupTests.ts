// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// Mock matchMedia globally for all tests
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // Deprecated
    removeListener: jest.fn(), // Deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock framer-motion components to strip animation props
const createMockMotionComponent = (element: string) => {
  return ({ children, whileHover, initial, animate, exit, layout, variants, transition, ...props }: any) => {
    const React = require('react');
    return React.createElement(element, props, children);
  };
};

jest.mock('framer-motion', () => ({
  motion: {
    div: createMockMotionComponent('div'),
    tr: createMockMotionComponent('tr'),
    span: createMockMotionComponent('span'),
    button: createMockMotionComponent('button'),
  },
  AnimatePresence: ({ children }: any) => children,
}));

// Mock @mui/material useMediaQuery hook to always return false by default
jest.mock('@mui/material', () => {
  const originalModule = jest.requireActual('@mui/material');
  return {
    ...originalModule,
    useMediaQuery: jest.fn(() => false),
  };
});

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  constructor() {}
  observe() { return null; }
  disconnect() { return null; }
  unobserve() { return null; }
};

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() {}
  observe() { return null; }
  disconnect() { return null; }
  unobserve() { return null; }
};