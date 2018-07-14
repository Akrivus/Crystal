require 'zip'
system("gradlew build")
Dir.entries("./lib").each do |file|
	begin
		Zip::File.open(file) do |zip|
			zip.each do |member|
				path = File.join("./mod", member.name)
				zip.extract(member, path)
			end
		end
	rescue => e
	end
end
Zip::File.open("./build/libs/Clones-1.0.jar") do |zip|
	zip.each do |member|
		path = File.join("./mod", member.name)
		zip.extract(member, path)
	end
end
