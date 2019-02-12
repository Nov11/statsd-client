package io.github.nov11;

public class TimGroupStatsDClient extends BaseClientImpl {

    public TimGroupStatsDClient(String prefix, MetricSender sender) {
        super(sender);
        if (prefix == null || prefix.equals("")) {
            this.prefix = "";
        } else {
            this.prefix = prefix + '.';
        }
    }
}
