# Accessibility

## Manual Testing (WCAG 2.1 - AA)

Manual Testing (WCAG 2.1 - AA)
For manual testing, Microsoft Accessibility Insights for Web has been used, which comprises 25 tests in various categories.

Tools:

1. Accessibility Insights for Web plugin ([link](https://accessibilityinsights.io/docs/web/overview/))

Steps:

- Install the plugin and open it on the local or online version of publisher
- Run the `Assessment` tests

Expected Outcome: All 25 tests should pass for the HtmlPublisher to conform to WCAG 2.1 level AA.

## Visual (Colorblind)

### Introduction

Someone who is colorblind can still see pigmentation but will have trouble distinguishing between colors.

Red-green color blindness is the most common and makes it difficult for a person to distinguish between red and green. Green can often look red and red can look like a dull green.

Blue-yellow color blindness is less common and as the name suggests, causes someone to confuse blue and yellow with other colors.

And, in rare cases, a person could have monochromacy, which means they cannot see any colors at all.

### Why it is important?

The most common form of color blindness affects 1 in 12 men and 1 in 200 women.

### Test Cases

#### Test 1: Color Alone Conveying Important Information

Objective: Determine if important information relies solely on color.

Tools:

1. High Contrast Chrome plugin ([link](https://chromewebstore.google.com/detail/high-contrast/djcfdncoelnlbldjfhinnjlhdjlikmph))
2. Total Colorfilter ([link](http://www.toptal.com/designers/colorfilter/))

Steps:

- View the webpage in grayscale mode (this can usually be done through browser settings or accessibility tools).
- Review all information and functionalities on the page.
- Note if any crucial information or functions are only conveyed through color.

Expected Outcome: All information and functions should still be understandable and accessible in grayscale mode.

Requirement: Conforms to SC 1.4.1 in WCAG 2.0, 2.1, 2.2.
