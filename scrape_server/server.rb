
imports = {
"sinatra"  => (require "sinatra"),
"nokogiri" => (require "nokogiri"),
"net/http" => (require "net/http"),
"time"     => (require "time"),
"json"     => (require "json"),
"open-uri" => (require "open-uri")
}

puts imports

get "/scrape" do
	content_type :json
	puts params
	if params.has_key? "url" and params.has_key? "columns" and params.has_key? "rules"
		rules   = params["rules"].split("*,")
		columns = params["columns"].split("*,")
		webpage = Nokogiri::HTML::Document.parse(open(params["url"]))
		if rules.length == columns.length
			unordered_result = []
			result           = []
			columns.zip(rules).each do |key, value|
				unordered_result << webpage.css(value).map {|x| x.text}
			end
			unordered_result.transpose.map do |member|
				temp = {}
				columns.zip(member).each do |k, v|
					temp[k] = v
				end
				result << temp
			end
			return {:status => :SUCCESS, :data => result}.to_json
		else
			return {:status => :ERROR, :reason => :PARAMS_LENGTH}.to_json
		end
	else
		return {:status => :ERROR, :reason => :PARAMS}.to_json
	end
end
