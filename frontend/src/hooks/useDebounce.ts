import { useState, useEffect } from 'react';

/**
 * Custom hook that debounces a value by delaying its update until after a specified delay
 * @param value - The value to debounce
 * @param delay - The delay in milliseconds
 * @returns The debounced value
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    // Set up a timer to update the debounced value after the delay
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // Clean up the timer if the value changes before the delay is complete
    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
}