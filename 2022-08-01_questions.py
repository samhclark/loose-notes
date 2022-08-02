#!/usr/bin/env python3
import sys

# Determine if a string has all unique characters
# O (n)
def isUnique(s) -> bool:
    seenChars = set()
    for c in s:
        if c in seenChars:
            return False
        else:
            seenChars.add(c)
    return True

# O (n log n + n)
def isUniqueNoExtraData(s) -> bool:
    prev = ''
    for c in sorted(s):
        if c == prev:
            return False
    return True

# Check if two strings are permutations of each other
# O ( n log n + m log m + n)
# probably better time complexity to build sets
def isPermutation(a, b) -> bool:
    a_chars = sorted(a)
    b_chars = sorted(b)
    return (a_chars == b_chars)

# O (n + m + n?)
def isPermutationWithSets(a, b) -> bool:
    a_chars = { c for c in a }
    b_chars = { c for c in b }
    return a_chars == b_chars

def main():
    a = sys.argv[1]
    b = sys.argv[2]
    # if isUniqueNoExtraData(a):
    if isPermutationWithSets(a, b):
        # print(f"{a} is unique")
        print(f"{a} is a permutation of {b}")
    else:
        # print(f"{a} is not unique")
        print(f"{a} is not a permutation of {b}")

if __name__ == '__main__':
    main()