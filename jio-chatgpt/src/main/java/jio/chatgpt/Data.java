package jio.chatgpt;

public class Data {


    public enum IMAGE_FORMAT {url, b64_json}

    public enum IMAGE_SIZE {
        _256("256x256"),
        _512("512x512"),
        _1024("1024x1024");

        public final String size;

        IMAGE_SIZE(String size) {
            this.size = size;
        }
    }

    public enum RESPONSE_FORMAT {json, text, srt, verbose_json, vtt}

    public enum ROLE {system, user, assistant}


}
