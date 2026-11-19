package com.example.util

import com.example.i18n.AppLanguage
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
        "heeb(?:s)?",

        // Restricted phrases, sexual slang, and brainrot/troll terms
        "cash[\\s._\\-*]+is[\\s._\\-*]+[ck]lay",
        "cashis[ck]lay",
        "nuh+",
        "cuh+(?:z|zz)?",
        "nut(?:s|t|ted|ting|z)?",
        "nut[\\s._\\-*]*sack(?:s)?",
        "deez[\\s._\\-*]*nut(?:s|z)?",
        "deeznut(?:s|z)?",
        "c[u*]m+(?:ming|med|mer|mers|shot|shots|slut|sluts|dumpster|dumpsters|stain|stains|rag|rags|s)?",
        "jizz+(?:ed|ing|er|s)?",
        "splooge(?:d|ing|s)?",
        "skeet(?:ed|ing|s)?",
        "semen",
        "sperm",
        "ejaculat(?:e|ed|ing|ion|ions)",
        "masturbat(?:e|ed|ing|ion)",
        "fap+(?:ed|ing|s)?",
        "orgasm(?:s)?",
        "boner(?:s)?",
        "horny",
        "porn(?:o|ography)?",
        "hentai",
        "dildo(?:s)?",
        "penis(?:es)?",
        "vagina(?:s)?",
        "clit(?:s|oris)?",
        "boob(?:s|ies|y)?",
        "tit(?:s|ty|ties)",
        "handjob(?:s)?",
        "blowjob(?:s)?",
        "rimjob(?:s)?",
        "footjob(?:s)?",
        "anal",
        "anus",
        "scrotum",
        "testicle(?:s)?",
        "ballsack(?:s)?",
        "buss(?:y|ies)",
        "gyat+",
        "goon(?:er|ers|ing)",
        "skibidi",
        "thot(?:s)?",
        "skank(?:s|y)?",
        "milf(?:s)?",
        "dilf(?:s)?",
        "stfu",
        "gtfo",
        "fuq",
        "fck",
        "shyt+",
        "shite",
        "bich(?:es)?",
        "biatch(?:es)?",
        "bytch(?:es)?",
        "azz+",
        "ashole(?:s)?",
        "cnt",
        "dik",
        "kok",
        "pusy"
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

    // US/Canadian and international phone numbers (avoids false positives on Steam AppIDs, years, and specs)
    private val PHONE_NUMBER_REGEX = Regex(
        pattern = "(?:" +
            "(?:\\+?1[-.\\s]*)?(?:\\(\\d{3}\\)|\\d{3})[-.\\s]*\\d{3}[-.\\s]*\\d{4}" +
            "|\\+\\d{1,3}(?:[-.\\s()]*\\d){7,13}" +
            "|\\b(?:call|text|phone|tel|dial|sms|ring|whatsapp|facetime)\\b[^\\n\\d]{0,20}\\d{3}[-.\\s]\\d{4}" +
            "|\\b\\d{3}[-.]\\d{4}" +
            ")\\b(?!\\s*(?:fps|hz|hours?|hrs?|players?|gamers?|mb|gb|tb|xp|gold|coins?|points?|pts)\\b)",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    // Physical street addresses (e.g. "123 Main St", "742 Evergreen Terrace", "221B Baker Street")
    private val STREET_ADDRESS_REGEX = Regex(
        pattern = "\\b\\d{1,6}[a-z]?\\s+" +
            "(?:(?:north|south|east|west|n\\.?|s\\.?|e\\.?|w\\.?|ne|nw|se|sw)\\s+)?" +
            "(?!(?:hours?|hrs?|mins?|minutes?|secs?|seconds?|days?|weeks?|months?|years?|fps|hz|gb|tb|mb|kb|pb|players?|gamers?|fans?|users?|times?|ways?|stars?|bucks|dollars?|coins?|points?|levels?|miles?|percent)\\b)" +
            "(?:[a-z0-9'.\\-]+\\s+){1,3}" +
            "(?:street|st|avenue|ave|boulevard|blvd|road|rd|drive|dr|lane|ln|court|ct|terrace|ter|trail|trl|parkway|pkwy|highway|hwy|circle|cir|crescent|cres|plaza|plz|alley)\\b\\.?" +
            "(?:\\s+(?:n|s|e|w|ne|nw|se|sw)\\b)?" +
            "(?:[,\\s]+(?:apt|apartment|suite|ste|unit|#)\\s*[a-z0-9-]+)?",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    private val SAFE_ADDRESS_FALSE_POSITIVE_REGEX = Regex(
        pattern = "\\b(?:hard\\s+drive|solid\\s+state\\s+drive|ssd\\s+drive|hdd\\s+drive|nvme\\s+drive|usb\\s+drive|flash\\s+drive|external\\s+drive|internal\\s+drive|disk\\s+drive|disc\\s+drive|optical\\s+drive|thumb\\s+drive|test\\s+drive|warp\\s+drive|hyper\\s+drive|on\\s+the\\s+road|down\\s+the\\s+road)\\b",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    private val PO_BOX_REGEX = Regex(
        pattern = "\\bP\\.?\\s*O\\.?\\s*Box\\s+#?[a-z0-9-]{1,10}\\b",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    private val CITY_STATE_ZIP_REGEX = Regex(
        pattern = "\\b[a-z][a-z\\s.'-]{1,25},\\s*(?:AL|AK|AZ|AR|CA|CO|CT|DE|FL|GA|HI|ID|IL|IN|IA|KS|KY|LA|ME|MD|MA|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY|DC)\\s+\\d{5}(?:-\\d{4})?\\b",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    private val EMAIL_ADDRESS_REGEX = Regex(
        pattern = "\\b[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}\\b",
        options = setOf(RegexOption.IGNORE_CASE)
    )

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

    fun containsPhoneNumber(text: String): Boolean {
        if (text.isBlank()) return false
        val cleaned = ZERO_WIDTH_REGEX.replace(text, "")
        return PHONE_NUMBER_REGEX.containsMatchIn(cleaned)
    }

    fun containsAddress(text: String): Boolean {
        if (text.isBlank()) return false
        val cleaned = ZERO_WIDTH_REGEX.replace(text, "")
        if (PO_BOX_REGEX.containsMatchIn(cleaned) ||
            CITY_STATE_ZIP_REGEX.containsMatchIn(cleaned) ||
            EMAIL_ADDRESS_REGEX.containsMatchIn(cleaned)
        ) {
            return true
        }
        return STREET_ADDRESS_REGEX.findAll(cleaned).any { match ->
            !SAFE_ADDRESS_FALSE_POSITIVE_REGEX.containsMatchIn(match.value)
        }
    }

    private fun containsProfanityOrSlur(text: String): Boolean {
        if (text.isBlank()) return false
        return buildNormalizedVariants(text).any { variant ->
            DIRECT_WORD_REGEX.containsMatchIn(variant)
        }
    }

    /**
     * Returns true if the supplied text contains any blacklisted swear, slur, slang, address, or phone number.
     */
    fun containsBlacklistedWords(text: String): Boolean {
        if (text.isBlank()) return false
        return containsProfanityOrSlur(text) || containsPhoneNumber(text) || containsAddress(text)
    }

    /**
     * Validates title, comment, and authorName before posting a review.
     */
    fun validateReviewInput(
        title: String,
        comment: String,
        authorName: String,
        isSpanish: Boolean = false
    ): ValidationResult = validateReviewInput(
        title = title,
        comment = comment,
        authorName = authorName,
        language = if (isSpanish) AppLanguage.SPANISH else AppLanguage.ENGLISH
    )

    fun validateReviewInput(
        title: String,
        comment: String,
        authorName: String,
        language: AppLanguage
    ): ValidationResult {
        val isEs = language == AppLanguage.SPANISH
        val isFr = language == AppLanguage.FRENCH
        val cleanTitle = title.trim()
        val cleanComment = comment.trim()
        val cleanAuthor = authorName.trim()

        if (cleanComment.isBlank() && cleanTitle.isBlank()) {
            return ValidationResult(
                isAllowed = false,
                errorMessage = when {
                    isEs -> "Por favor escribe un título o comentario."
                    isFr -> "Veuillez écrire un titre ou un commentaire."
                    else -> "Please write a review headline or comment."
                }
            )
        }

        val flagged = mutableListOf<String>()
        val titleHasPii = containsAddress(cleanTitle) || containsPhoneNumber(cleanTitle)
        val commentHasPii = containsAddress(cleanComment) || containsPhoneNumber(cleanComment)
        val authorHasPii = containsAddress(cleanAuthor) || containsPhoneNumber(cleanAuthor)

        if (titleHasPii || containsProfanityOrSlur(cleanTitle)) {
            flagged.add(
                when {
                    isEs -> "título"
                    isFr -> "titre"
                    else -> "headline"
                }
            )
        }
        if (commentHasPii || containsProfanityOrSlur(cleanComment)) {
            flagged.add(
                when {
                    isEs -> "comentario"
                    isFr -> "commentaire"
                    else -> "review comment"
                }
            )
        }
        if (authorHasPii || containsProfanityOrSlur(cleanAuthor)) {
            flagged.add(
                when {
                    isFr -> "pseudo"
                    else -> "gamer tag"
                }
            )
        }

        if (flagged.isNotEmpty()) {
            val fieldList = when {
                isEs -> when (flagged.size) {
                    1 -> flagged[0]
                    2 -> "${flagged[0]} y ${flagged[1]}"
                    else -> "${flagged[0]}, ${flagged[1]} y ${flagged[2]}"
                }
                isFr -> when (flagged.size) {
                    1 -> flagged[0]
                    2 -> "${flagged[0]} et ${flagged[1]}"
                    else -> "${flagged[0]}, ${flagged[1]} et ${flagged[2]}"
                }
                else -> when (flagged.size) {
                    1 -> flagged[0]
                    2 -> "${flagged[0]} and ${flagged[1]}"
                    else -> "${flagged[0]}, ${flagged[1]}, and ${flagged[2]}"
                }
            }
            val hasPii = titleHasPii || commentHasPii || authorHasPii
            val message = when {
                isEs -> if (hasPii) {
                    "Se detectó contenido restringido (direcciones o teléfonos) en tu $fieldList. No se permiten datos personales ni groserías en reseñas."
                } else {
                    "Se detectaron palabras prohibidas en tu $fieldList. No se permiten groserías ni términos ofensivos en reseñas."
                }
                isFr -> if (hasPii) {
                    "Contenu restreint détecté (adresses ou numéros de téléphone) dans votre $fieldList. Les données personnelles et propos inappropriés sont interdits dans les avis."
                } else {
                    "Mots interdits détectés dans votre $fieldList. Les insultes et termes offensants ne sont pas autorisés dans les avis."
                }
                else -> if (hasPii) {
                    "Blacklisted content (addresses or phone numbers) detected in your $fieldList. Addresses, phone numbers, swears, slurs, and restricted slang are not allowed in community reviews."
                } else {
                    "Blacklisted words detected in your $fieldList. Swears, slurs, and restricted slang are not allowed in community reviews."
                }
            }
            return ValidationResult(
                isAllowed = false,
                errorMessage = message,
                flaggedFields = flagged
            )
        }

        return ValidationResult(isAllowed = true)
    }

    /**
     * Masks blacklisted words, addresses, and phone numbers in stored reviews before rendering.
     */
    fun sanitizeForDisplay(text: String): String {
        if (!containsBlacklistedWords(text)) return text

        var sanitized = PO_BOX_REGEX.replace(text, "[redacted]")
        sanitized = CITY_STATE_ZIP_REGEX.replace(sanitized, "[redacted]")
        sanitized = EMAIL_ADDRESS_REGEX.replace(sanitized, "[redacted]")
        sanitized = STREET_ADDRESS_REGEX.replace(sanitized) { match ->
            if (SAFE_ADDRESS_FALSE_POSITIVE_REGEX.containsMatchIn(match.value)) match.value else "[redacted]"
        }
        sanitized = PHONE_NUMBER_REGEX.replace(sanitized, "[redacted]")

        if (containsProfanityOrSlur(sanitized)) {
            sanitized = DIRECT_WORD_REGEX.replace(sanitized) { match ->
                maskMatch(match.value)
            }
        }

        if (containsProfanityOrSlur(sanitized)) {
            sanitized = INLINE_SEPARATOR_TOKEN_REGEX.replace(sanitized) { match ->
                if (containsProfanityOrSlur(match.value)) maskMatch(match.value) else match.value
            }
        }

        if (containsProfanityOrSlur(sanitized)) {
            sanitized = SEPARATED_LETTERS_REGEX.replace(sanitized) { match ->
                if (containsProfanityOrSlur(match.value)) "****" else match.value
            }
        }

        if (containsProfanityOrSlur(sanitized)) {
            sanitized = sanitized.split(Regex("(?<=\\s)|(?=\\s)")).joinToString("") { token ->
                if (token.isNotBlank() && containsProfanityOrSlur(token)) {
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
