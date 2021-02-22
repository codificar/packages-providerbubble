require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "RNProviderBubble"
  s.version      = "1.0.0"
  s.summary      = "RNProviderBubble"
  s.description  = "Bubble and Redis package used on logistic apps from codificar"
  s.homepage     = "https://git.codificar.com.br/packages/providerbubble"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNProviderBubble.git", :tag => "master" }
  s.source_files  = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.ios.deployment_target = '9.0'

  s.dependency "React"
  s.dependency "PSSRedisClient" 

  #s.dependency "others"

end

  