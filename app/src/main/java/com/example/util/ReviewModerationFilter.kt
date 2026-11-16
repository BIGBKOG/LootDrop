package com.example.util

import java.text.Normalizer

/**
 * Profanity and slur moderation filter for LootDrop community reviews.
 * Blocks reviews containing blacklisted swears or slurs (including common leetspeak,
 * symbol substitution, repeated characters, and separator obfuscation) and can mask
 * already-stored content on display while avoiding false positives on clean gaming terms.
 */
object ReviewModerationFilter {

    data class ValidationResult(
        val isAllowed: Boolean,
        val errorMessage: String? = null,
        val flaggedFields: List<String> = emptyList()
    )

    private val PROFANITY_AND_SLUR_TERMS: List<String> = listOf(
        // Profanity / swears (with common inflections)
        "fuck(?:er|ers|ed|ing|ings|in|wit|wits|head|heads|face|faces|tard|tards|boy|boys|s)?",
        "(?:mother|motha|mutha)fuck(?:er|ers|ed|ing|a|as|s)?",
        "clusterfuck(?:ed|s)?",
        "fvck(?:er|ers|ed|ing|s)?",
        "fuk(?:c|k)?(?:er|ers|ed|ing|s)?",
        "phuck(?:er|ers|ed|ing|s)?",
        "phuk(?:er|ers|ed|ing|s)?",
        "f[*@#_]ck(?:er|ers|ed|ing|s)?",
        "f[*_]{2}k(?:er|ers|ed|ing|s)?",
        "shit(?:s|ty|tier|tiest|ted|ting|head|heads|face|faces|hole|holes|load|loads|show|shows|storm|storms|post|posts)?",
        "sh[*!_#]t(?:s|ty|ted|ting|head|heads)?",
        "bullshit(?:ter|ters|ted|ting|s)?",
        "horseshit",
        "dipshit(?:s)?",
        "batshit",
        "apeshit",
        "bitch(?:es|ed|ing|y|ass)?",
        "b[*!_#]tch(?:es|ed|ing|y)?",
        "son\\s+of\\s+a\\s+bitch",
        "bastard(?:s|ized)?",
        "cunt(?:s|y)?",
        "c[*_#]nt(?:s|y)?",
        "ass",
        "ass?hole(?:s)?",
        "arsehole(?:s)?",
        "jackass(?:es)?",
        "dumbass(?:es)?",
        "fatass(?:es)?",
        "smartass(?:es)?",
        "asswipe(?:s)?",
        "asshat(?:s)?",
        "assclown(?:s)?",
        "a[*_]{2}hole(?:s)?",
        "cock(?:s|sucker|suckers|sucking|head|heads|block|blocker|blockers)?",
        "c[*_#]ck(?:s|sucker|suckers|head|heads)?",
        "dick(?:s|head|heads|wad|wads|face|faces|weed|less)?",
        "d[*!_#]ck(?:s|head|heads|wad|wads|face|faces)?",
        "pussy",
        "pussies",
        "p[*_#]ss(?:y|ies)",
        "whore(?:s|d|ing|house|houses)?",
        "wh[*_#]re(?:s|d|ing)?",
        "slut(?:s|ty|tier|tiest)?",
        "sl[*_#]t(?:s|ty)?",
        "twat(?:s|waffle|waffles)?",
        "wank(?:er|ers|ed|ing|s)?",
        "prick(?:s)?",
        "douche(?:bag|bags|canoe|canoes|y|s)?",
        "piss(?:ed|ing|es)?",

        // Slurs & hate speech (racial, ethnic, religious, LGBTQ+, ableist)
        "nigg+(?:a|as|ah|ahs|er|ers|uh|uhs|let|lets)",
        "n[*!_#]gg(?:a|as|er|ers)",
        "nigr(?:a|o|os)",
        "negro(?:es|s)?",
        "sandnigg+(?:a|er|ers|as)",
        "fag+(?:ot|ots|otry|y|s)?",
        "f[*@_#]g(?:got|gots|s)?",
        "dyke(?:s)?",
        "trann(?:y|ies)",
        "shemale(?:s)?",
        "kike(?:s)?",
        "kyke(?:s)?",
        "spic(?:s|k|ks)?",
        "wetback(?:s)?",
        "beaner(?:s)?",
        "chink(?:s)?",
        "gook(?:s)?",
        "coon(?:s)?",
        "jigaboo(?:s)?",
        "sambo(?:s)?",
        "raghead(?:s)?",
        "towelhead(?:s)?",
        "paki(?:s)?",
        "retard(?:s|ed|ation)?",
        "r[*e]tard(?:s|ed)?",
        "libtard(?:s)?",
        "fucktard(?:s)?",
        "spastic(?:s)?",
        "mongoloid(?:s)?",
        "zipperhead(?:s)?",
        "porchmonkey(?:s)?",
        "junglebunny",
        "junglebunnies",
        "cameljockey(?:s)?",
        "redskin(?:s)?",
        "squaw(?:s)?",
        "injun(?:s)?",
        "wop(?:s)?",
        "dago(?:s|es)?",
        "polack(?:s)?",
        "kraut(?:s)?",
        "heeb(?:s)?"
    )

    private val DIRECT_WORD_REGEX = Regex(
        pattern = "\\b(?:${PROFANITY_AND_SLUR_TERMS.joinToString("|")})\\b",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    // Matches obfuscated single-letter runs separated by spaces or punctuation (e.g. "f u c k", "s.h.i.t")
    private val SEPARATED_LETTERS_REGEX = Regex(
        pattern = "(?<![\\p{L}\\p{N}])([\\p{L}\\p{N}@$!*])(?:[\\s._\\-*]+([\\p{L}\\p{N}@$!*])){2,}(?![\\p{L}\\p{N}])"
    )

    // Matches inline punctuation separators inside a single token (e.g. "f.u.c.k", "s-h-i-t")
    private val INLINE_SEPARATOR_TOKEN_REGEX = Regex(
        pattern = "(?<!\\S)[\\p{L}\\p{N}@$!*]+(?:[._\\-*]+[\\p{L}\\p{N}@$!*]+)+(?!\\S)"
    )

    private val CAMEL_CASE_SPLIT_REGEX = Regex("([\\p{Ll}\\p{N}@$!*#])([\\p{Lu}])")
    private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val ZERO_WIDTH_REGEX = Regex("[\\u200B-\\u200D\\uFEFF]")
    private val REPEATED_3_PLUS_REGEX = Regex("(.)\\1{2,}")
    private val REPEATED_2_PLUS_REGEX = Regex("(.)\\1+")

    private fun stripDiacriticsAndZeroWidth(input: String, splitCamelCase: Boolean = false): String {
        val noZeroWidth = ZERO_WIDTH_REGEX.replace(input, "")
        val decomposed = Normalizer.normalize(noZeroWidth, Normalizer.Form.NFKD)
        val noDiacritics = DIACRITICS_REGEX.replace(decomposed, "")
        val processed = if (splitCamelCase) {
            CAMEL_CASE_SPLIT_REGEX.replace(noDiacritics, "$1 $2")
        } else {
            noDiacritics
        }
        return processed.lowercase()
    }

    private fun normalizeLeetspeak(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '0' -> sb.append('o')
                '1', '!' -> sb.append('i')
                '3' -> sb.append('e')
                '4', '@' -> sb.append('a')
                '5', '$' -> sb.append('s')
                '7', '+' -> sb.append('t')
                '8' -> sb.append('b')
                '#' -> sb.append('h')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun buildNormalizedVariants(text: String): List<String> {
        val base = stripDiacriticsAndZeroWidth(text, splitCamelCase = false)
        val camelBase = stripDiacriticsAndZeroWidth(text, splitCamelCase = true)
        val leet = normalizeLeetspeak(base)
        val camelLeet = normalizeLeetspeak(camelBase)

        val variants = linkedSetOf(
            base,
            camelBase,
            leet,
            camelLeet,
            REPEATED_3_PLUS_REGEX.replace(base, "$1"),
            REPEATED_3_PLUS_REGEX.replace(leet, "$1"),
            REPEATED_3_PLUS_REGEX.replace(camelLeet, "$1"),
            REPEATED_3_PLUS_REGEX.replace(base, "$1$1"),
            REPEATED_3_PLUS_REGEX.replace(leet, "$1$1"),
            REPEATED_2_PLUS_REGEX.replace(base, "$1"),
            REPEATED_2_PLUS_REGEX.replace(leet, "$1"),
            REPEATED_2_PLUS_REGEX.replace(camelLeet, "$1")
        )

        // Collapse inline separator tokens (f.u.c.k -> fuck, f-u-c-k -> fuck)
        val collapsedInlineBase = INLINE_SEPARATOR_TOKEN_REGEX.replace(base) { match ->
            match.value.replace(Regex("[._\\-*]+"), "")
        }
        val collapsedInlineLeet = normalizeLeetspeak(collapsedInlineBase)
        variants.add(collapsedInlineBase)
        variants.add(collapsedInlineLeet)
        variants.add(REPEATED_2_PLUS_REGEX.replace(collapsedInlineLeet, "$1"))

        // Collapse separated single-letter sequences ("f u c k" -> "fuck", "s h 1 t" -> "shit")
        val collapsedSpacedBase = SEPARATED_LETTERS_REGEX.replace(base) { match ->
            match.value.replace(Regex("[\\s._\\-*]+"), "")
        }
        val collapsedSpacedLeet = normalizeLeetspeak(collapsedSpacedBase)
        variants.add(collapsedSpacedBase)
        variants.add(collapsedSpacedLeet)
        variants.add(REPEATED_2_PLUS_REGEX.replace(collapsedSpacedLeet, "$1"))

        return variants.toList()
    }

    /**
     * Returns true if the supplied text contains any blacklisted swear or slur.
     */
    fun containsBlacklistedWords(text: String): Boolean {
        if (text.isBlank()) return false
        return buildNormalizedVariants(text).any { variant ->
            DIRECT_WORD_REGEX.containsMatchIn(variant)
        }
    }

    /**
     * Validates title, comment, and authorName before posting a review.
     */
    fun validateReviewInput(
        title: String,
        comment: String,
        authorName: String
    ): ValidationResult {
        val cleanTitle = title.trim()
        val cleanComment = comment.trim()
        val cleanAuthor = authorName.trim()

        if (cleanComment.isBlank() && cleanTitle.isBlank()) {
            return ValidationResult(
                isAllowed = false,
                errorMessage = "Please write a review headline or comment."
            )
        }

        val flagged = mutableListOf<String>()
        if (containsBlacklistedWords(cleanTitle)) flagged.add("headline")
        if (containsBlacklistedWords(cleanComment)) flagged.add("review comment")
        if (containsBlacklistedWords(cleanAuthor)) flagged.add("gamer tag")

        if (flagged.isNotEmpty()) {
            val fieldList = when (flagged.size) {
                1 -> flagged[0]
                2 -> "${flagged[0]} and ${flagged[1]}"
                else -> "${flagged[0]}, ${flagged[1]}, and ${flagged[2]}"
            }
            return ValidationResult(
                isAllowed = false,
                errorMessage = "Blacklisted words detected in your $fieldList. Swears and slurs are not allowed in community reviews.",
                flaggedFields = flagged
            )
        }

        return ValidationResult(isAllowed = true)
    }

    /**
     * Masks blacklisted words in stored reviews before rendering to keep display clean.
     */
    fun sanitizeForDisplay(text: String): String {
        if (!containsBlacklistedWords(text)) return text

        var sanitized = DIRECT_WORD_REGEX.replace(text) { match ->
            maskMatch(match.value)
        }

        if (containsBlacklistedWords(sanitized)) {
            sanitized = INLINE_SEPARATOR_TOKEN_REGEX.replace(sanitized) { match ->
                if (containsBlacklistedWords(match.value)) maskMatch(match.value) else match.value
            }
        }

        if (containsBlacklistedWords(sanitized)) {
            sanitized = SEPARATED_LETTERS_REGEX.replace(sanitized) { match ->
                if (containsBlacklistedWords(match.value)) "****" else match.value
            }
        }

        if (containsBlacklistedWords(sanitized)) {
            sanitized = sanitized.split(Regex("(?<=\\s)|(?=\\s)")).joinToString("") { token ->
                if (token.isNotBlank() && containsBlacklistedWords(token)) {
                    maskMatch(token)
                } else {
                    token
                }
            }
        }

        return sanitized
    }

    private fun maskMatch(token: String): String {
        val trimmed = token.trim()
        if (trimmed.length <= 2) return "*".repeat(trimmed.length.coerceAtLeast(2))
        return trimmed.first() + "*".repeat(trimmed.length - 1)
    }
}
