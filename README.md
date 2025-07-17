## WatchTower.AI *MCP First-Principles* Talk

### 📊 Presentation Slides

**[🎯 View Live Presentation](https://htmlpreview.github.io/?https://github.com/abhisheksp/mcp-first-principles/blob/main/slides/slides.html)**

*Or download and open [slides/slides.html](slides/slides.html) locally in your browser*

The presentation includes animations and visual explanations of the MCP evolution from first principles.

### 1  Why this repo exists

This repository is the companion for a 45-minute talk (+15 Q\&A) that walks engineers from "one-off AWS demo" to "fully-fledged protocol (MCP)".
Each git branch represents a chapter in that story; the code is illustrative, but the **learning happens in the journey between branches**.

### 2  How to follow along during the talk

| Phase | Git branch                       | High-level theme                        | Speaker resource    |
| ----- | -------------------------------- | --------------------------------------- | ------------------- |
| 0     | **main**                         | Skeleton Maven project                  | *Phase-0-prompt.md* |
| 1     | **01-aws-mvp**                   | Hard-coded AWS MVP                      | *Phase-1-prompt.md* |
| 2     | **02-gcp-pressure**              | Quick-fix GCP support → copy-paste pain | *Phase-2-prompt.md* |
| 3     | **03-extract-interface**         | Extract common interface                | *Phase-3-prompt.md* |
| 4     | **04-function-calling**          | LLM function-calling revolution         | *Phase-4-prompt.md* |
| 5     | **05-transport-standardization** | Transport multiplication problem        | *Phase-5-prompt.md* |
| 6     | **06-protocol-revelation**       | One protocol → MCP connection           | *Phase-6-prompt.md* |

> **Tip for attendees**
>
> ```
> git clone <repo>
> git checkout 03-extract-interface   # or any phase you want
> docs/03-extract-interface.md        # slide-style notes / speaker narrative
> ```

### 3  Repo anatomy

```
.
├── slides/                     # Complete HTML presentation with images
├── docs/                       # One markdown "slide deck" per phase
├── claude-code-full-prompts/   # Full Claude Code prompts for each phase
├── src/main/java/…             # Illustrative implementation per branch
└── src/test/java/…             # Demo tests you'll run live
```

*If you want the gritty implementation details, open the matching prompt file or branch.*

### 4  Running the demos live

1. **Open the presentation**: Start with the [live slides](https://htmlpreview.github.io/?https://github.com/abhisheksp/mcp-first-principles/blob/main/slides/slides.html) for the visual narrative
2. **Check out the branch for the phase you're discussing.**
3. `mvn test -q` to run the illustrative JUnit scenarios; they log rich console output.
4. Use `git diff` between phases to *show, not tell* how design pressure drives refactors.

> **Speaker tip**: The slides and code work together - use slides to explain concepts, then switch to the terminal to show the actual implementation evolving through the branches.

### 5  About the Claude.ai prompts

Toward the end of **Phase 5** the Claude chat hit the token-limit wall. The prompt in *Phase-5-prompt.md* contains minor manual edits where context had to be re-introduced before continuing.&#x20;

### 6  Full design conversation (placeholder)

For transparency, the entire Claude.ai session is shared here:
https://claude.ai/share/5ce77a82-5a02-4206-bf20-ed9b0d03a1f6
