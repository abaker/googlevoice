package com.github.abaker.googlevoice.proto

import Voice
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import timber.log.Timber

fun <T : Message> parseData(c: Class<T>, data: String): T {
    val response = c.declaredConstructors
        .firstOrNull { it.parameters.isEmpty() }
        ?.apply { isAccessible = true }
        ?.newInstance() as? T
        ?: throw RuntimeException("Missing constructor")
    val builder = response.toBuilder()
    val descriptor = response.descriptorForType
    val tokens = tokenizeString(data.trim())
    for (i in 1 .. descriptor.fields.size) {
        val field = descriptor.findFieldByNumber(i)
        val value = tokens.getOrNull(i-1) ?: continue
        val isNull = value == "null"
        if (isNull) {
            println("$field is null")
            continue
        } else {
            println("parsing ${field.type} $field=$value")
        }
        val parser: (String) -> Any? = {
            when (field.type) {
                Descriptors.FieldDescriptor.Type.ENUM -> {
                    val enumDescriptor = when (field.enumType.fullName) {
                        "MessageType" -> Voice.MessageType.getDescriptor()
                        "MessageStatus" -> Voice.MessageStatus.getDescriptor()
                        "View" -> Voice.View.getDescriptor()
                        "CoarseType" -> Voice.CoarseType.getDescriptor()
                        "TranscriptStatus" -> Voice.TranscriptStatus.getDescriptor()
                        "FeedbackStatus" -> Voice.FeedbackStatus.getDescriptor()
                        "MmsStatus" -> Voice.MmsStatus.getDescriptor()
                        "MediaType" -> Voice.MediaType.getDescriptor()
                        else -> throw IllegalArgumentException()
                    }
                    enumDescriptor.findValueByNumber(it.toInt())
                }
                Descriptors.FieldDescriptor.Type.MESSAGE -> {
                    parseData(
                        c = when (field.messageType.fullName) {
                            "Thread" -> Voice.Thread::class.java
                            "Message" -> Voice.Message::class.java
                            "MmsMessage" -> Voice.MmsMessage::class.java
                            "Contact" -> Voice.Contact::class.java
                            "UserFeedback" -> Voice.UserFeedback::class.java
                            "Attachment" -> Voice.Attachment::class.java
                            "ImageMetaData" -> Voice.ImageMetaData::class.java
                            else -> throw IllegalArgumentException()
                        },
                        data = it
                    )
                }
                Descriptors.FieldDescriptor.Type.BOOL -> it == "1"
                Descriptors.FieldDescriptor.Type.STRING -> {
                    it
                }
                Descriptors.FieldDescriptor.Type.INT32 -> it.toInt()
                Descriptors.FieldDescriptor.Type.INT64 -> it.toLong()
                else -> {
                    Timber.w("Unhandled ${field.type}: $field=$value")
                    null
                }
            }
        }
        val v = if (field.isRepeated) {
            tokenizeString(value).mapNotNull { parser(it) }
        } else {
            parser(value)
        }
        builder.setField(
            field,
            v
        )
    }
    return builder.build() as T
}

fun tokenizeString(input: String): List<String> {
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var bracketLevel = 0
    if (!input.startsWith("[")) {
        throw IllegalArgumentException("Does not start with [: $input")
    }
    if (!input.endsWith("]")) {
        throw IllegalArgumentException("end token ${input.last()} != ]: $input")
    }
    for (char in input.substring(1, input.length - 1)) {
        when {
            char == '"' -> {
                if (inQuotes && sb.lastOrNull() == '\\') {
                    sb.append(char)
                } else {
                    if (bracketLevel > 0) {
                        sb.append(char)
                    }
                    inQuotes = !inQuotes
                }
            }
            inQuotes -> sb.append(char)
            char == '[' -> {
                bracketLevel++
                if (bracketLevel > 0) {
                    sb.append(char)
                }
            }
            char == ']' -> {
                if (bracketLevel > 0) {
                    sb.append(char)
                }
                bracketLevel--
            }
            char == ',' && bracketLevel == 0 -> {
                tokens.add(sb.toString().trim())
                sb.clear()
            }
            else -> sb.append(char)
        }
    }
    tokens.add(sb.toString().trim())
    println("tokens=$tokens")
    return tokens
}
