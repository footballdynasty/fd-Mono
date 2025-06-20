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

// Mock framer-motion globally for all tests to avoid animation issues
jest.mock('framer-motion', () => {
  const React = require('react');
  return {
    motion: {
      div: ({ children, whileHover, initial, animate, exit, layout, variants, transition, ...props }: any) => 
        React.createElement('div', props, children),
      tr: ({ children, whileHover, initial, animate, exit, layout, variants, transition, ...props }: any) => 
        React.createElement('tr', props, children),
      span: ({ children, whileHover, initial, animate, exit, layout, variants, transition, ...props }: any) => 
        React.createElement('span', props, children),
      button: ({ children, whileHover, initial, animate, exit, layout, variants, transition, ...props }: any) => 
        React.createElement('button', props, children),
    },
    AnimatePresence: ({ children }: any) => children,
  };
});

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  constructor() {}
  observe() {
    return null;
  }
  disconnect() {
    return null;
  }
  unobserve() {
    return null;
  }
};

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() {}
  observe() {
    return null;
  }
  disconnect() {
    return null;
  }
  unobserve() {
    return null;
  }
};