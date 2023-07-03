//
//  WidgetBundle.swift
//  NewsWidget
//
//  Created by Guts on 2023-02-27.
//  Copyright © 2023 Facebook. All rights reserved.
//

import WidgetKit
import SwiftUI

import Alamofire
import AlamofireRSSParser

extension Date {
  func timeAgoDisplay() -> String {
    let formatter = RelativeDateTimeFormatter()
    formatter.unitsStyle = .full
    return formatter.localizedString(for: self, relativeTo: Date())
  }
}

// todo: cache image
func loadImage(url: String) async -> Image {
  guard let imageURL = URL(string: url) else {
    return Image("Logo")
  }
  let request = URLRequest(url: imageURL)
  if let (data, _) = try? await URLSession.shared.data(for: request, delegate: nil) {
    return Image(uiImage: UIImage(data: data)!)
  } else {
    return Image("Logo")
  }
}

@available(iOS 13, *)
func swiftConcurrencyFetch(url: String) async -> RSSFeed? {
  let rss = await AF.request(url).serializingRSS().response.value
  return rss
}

@main
struct NewsWidgetBundle: WidgetBundle {
    var body: some Widget {
        WidgetNews()
        WidgetLiveActivity()
    }
}
