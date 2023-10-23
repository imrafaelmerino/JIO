/**
 * The `jio.chatgpt` package contains a set of classes and builders for interacting with the OpenAI GPT-based models to
 * perform various natural language processing and image generation tasks through API requests. These classes and
 * builders are designed to simplify the usage of the OpenAI API and provide convenient methods for creating and
 * managing requests for different tasks.
 * <p>
 * The key classes and builders in this package include: - {@link jio.chatgpt.ConfBuilder}: A configuration builder for
 * setting API authentication details and other settings. - {@link jio.chatgpt.ChatGPTServices}: A central class that provides
 * access to different service classes for various tasks, such as chat, image generation, transcription, and more. -
 * {@link jio.chatgpt.AudioService}: A service class for working with audio-related tasks, including transcription and
 * translation. - {@link jio.chatgpt.ChatService}: A service class for interactive conversations and chat-based tasks. -
 * {@link jio.chatgpt.TranscriptionBuilder}: A builder for creating transcription tasks, including options for prompts
 * and response formats. - {@link jio.chatgpt.TranslationBuilder}: A builder for creating translation tasks, including
 * response formats and temperature settings. - {@link jio.chatgpt.ImageBuilder}: A builder for creating images based on
 * prompts and settings. - {@link jio.chatgpt.VariationImageBuilder}: A builder for generating variations of images
 * based on an input image. - {@link jio.chatgpt.AbstractService}: An abstract base class for service classes, providing
 * common functionality for making API requests.
 *
 * @since 1.0
 */
package jio.chatgpt;