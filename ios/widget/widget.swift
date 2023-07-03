//
//  Widget.swift
//  Widget
//
//  Created by Guts on 2023-02-27.
//  Copyright © 2023 Facebook. All rights reserved.
//

import WidgetKit
import SwiftUI
import Intents

struct Article: Identifiable {
  let id: String
  let headline: String
  let pubDate: String
  let image: Image
  let link: String
}

struct TopNewsEntry: TimelineEntry {
  let date: Date
  var articles: [Article]
}

class DefaultEntryNews {
  static let id = "dde22d20-41ed-4f7d-b743-84488dcb35c7"
  static let defaultImage = Image("Logo")
  static let defaultHeadline = "Winter storm updates: Toronto declares ‘major snowstorm condition’ to help clear routes; heavy snow to be replaced by high winds"
  static let defaultPubDate = "4 Mar 2023"
  static let defaultLink = "https://www.thestar.com/news/gta/2023/03/04/toronto-storm-may-be-over-but-get-ready-for-high-winds.html"
  static let defaultEntry = TopNewsEntry(date: Date(), articles: [Article(id: id, headline: defaultHeadline, pubDate: defaultPubDate, image: defaultImage, link: defaultLink)])
}


struct Provider: IntentTimelineProvider {
  let rssURL = "https://www.thestar.com/content/thestar/feed.RSSManagerServlet.articles.topstories.rss"

  func placeholder(in context: Context) -> TopNewsEntry {
    DefaultEntryNews.defaultEntry
  }

  func getSnapshot(for configuration: ConfigurationIntent, in context: Context, completion: @escaping (TopNewsEntry) -> ()) {
    completion(DefaultEntryNews.defaultEntry)
  }

  func getTimeline(for configuration: ConfigurationIntent, in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
    Task {
      let entryDate = Date()
      if let rss = await swiftConcurrencyFetch(url: self.rssURL) {
        var count = 1
        if context.family == .systemLarge {
          count = 3
        }

        var entry = TopNewsEntry(date: entryDate, articles: [])
        for index in 0..<count {
          let topNews = rss.items[index]
          let id = topNews.guid ?? ""
          let headline = topNews.title ?? "-"
          let pubDate = topNews.pubDate ?? Date()
          let image = await loadImage(url: topNews.mediaThumbnail ?? "")
          let link = topNews.link ?? ""
          entry.articles.append(Article(id: id, headline: headline, pubDate: pubDate.timeAgoDisplay(), image: image, link: link))
        }

        let timeline = Timeline(entries: [entry], policy: .atEnd)
        completion(timeline)
      }
    }
  }
}

struct WidgetEntryView: View {

  @Environment(\.widgetFamily) var family: WidgetFamily
  let topNews: Provider.Entry

  @ViewBuilder
  var body: some View {
    switch family {
    case .systemSmall:
      SmallWidgetView(topNews: topNews)
    case .systemMedium:
      MediumWidgetView(topNews: topNews)
    case .systemLarge:
      LargeWidgetView(topNews: topNews)
    @unknown default:
      EmptyView()
    }
  }
}

struct SmallWidgetView: View {
  let topNews: Provider.Entry

  var body: some View {
    ZStack(alignment: .bottomLeading) {
      topNews.articles[0].image
        .resizable()
        .scaledToFill()
        .layoutPriority(-1)
        .clipped()
      Color.black.opacity(0.35)
      VStack(alignment: .listRowSeparatorLeading) {
        Text(topNews.articles[0].headline)
          .font(.subheadline)
          .bold()
          .lineLimit(3)
        //        Spacer()
        Text(topNews.articles[0].pubDate)
          .lineLimit(1)
          .fontWeight(.light)
          .font(.caption)
          .padding(.top, 0.5)

      }
      .foregroundColor(.white)
      .padding()
    }
    .widgetURL(URL(string: topNews.articles[0].link))
  }
}

struct MediumWidgetView: View {
  let topNews: Provider.Entry

  var body: some View {
    ZStack {
      Color(UIColor.systemBackground)
      VStack(alignment: .leading) {
        HStack(alignment: .top) {
          Text("Top Story")
            .font(.headline)
            .bold()
            .fontWeight(.heavy)
          HStack {
            Spacer()
          }
          Image("Logo")
            .resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: 20, height: 20)
            .clipped()
            .cornerRadius(4)
        }

        HStack(alignment: .top, spacing: 0) {
          topNews.articles[0].image
            .resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: 100, height: 100)
            .clipShape(ContainerRelativeShape())
          VStack(alignment: .listRowSeparatorLeading, spacing: 10) {
            Text(topNews.articles[0].headline)
              .font(.subheadline)
              .bold()
              .lineLimit(3)
//            HStack {
//              Spacer()
//            }
            Text(topNews.articles[0].pubDate)
              .lineLimit(1)
              .fontWeight(.light)
              .font(.caption)
          }
          .padding([.leading, .trailing])
        }

      }
      .padding([.leading, .trailing])
    }
    .widgetURL(URL(string: topNews.articles[0].link))
  }
}

struct LargeWidgetView: View {
  let topNews: Provider.Entry

  var body: some View {
    ZStack {
      Color(UIColor.systemBackground)
      VStack(alignment: .leading) {
        HStack(alignment: .top) {
          Text("Top Stories")
            .font(.headline)
            .bold()
            .fontWeight(.heavy)
          HStack {
            Spacer()
          }
          Image("Logo")
            .resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: 20, height: 20)
            .clipped()
            .cornerRadius(4)
        }
        .padding(.horizontal)

        ForEach(topNews.articles) { article in
          HStack(alignment: .top, spacing: 0) {
            article.image
              .resizable()
              .aspectRatio(contentMode: .fill)
              .frame(width: 80, height: 80)
              .clipped()
              .cornerRadius(4)
//              .clipShape(ContainerRelativeShape())
            VStack(alignment: .listRowSeparatorLeading, spacing: 5) {
              Text(article.headline)
                .font(.subheadline)
                .bold()
                .lineLimit(3)
//              HStack {
//                Spacer()
//              }
              Text(article.pubDate)
                .lineLimit(1)
                .fontWeight(.light)
                .font(.caption)
            }
            .padding([.leading, .trailing])
          }
          .padding([.leading, .trailing])
          .widgetURL(URL(string: article.link))

        }
      }
    }
  }
}

struct WidgetNews: Widget {
  let kind: String = "Widget"

  var body: some WidgetConfiguration {
    IntentConfiguration(kind: kind, intent: ConfigurationIntent.self, provider: Provider()) { entry in
      WidgetEntryView(topNews: entry)
    }
    .supportedFamilies([.systemSmall,.systemMedium,.systemLarge])
    .configurationDisplayName("Star Top News Widget")
    .description("Display Top News from Toronto Star RSS")
  }
}

struct WidgetNews_Previews: PreviewProvider {
  static var previews: some View {
    ForEach(
      [
        WidgetFamily.systemSmall,
        WidgetFamily.systemMedium,
        WidgetFamily.systemLarge,
        // Add Support to Lock Screen widgets
//        .accessoryCircular,
//        .accessoryRectangular,
//        .accessoryInline,
      ], id: \.self) { family in
        WidgetEntryView(
          topNews: DefaultEntryNews.defaultEntry
        )
        .previewContext(
          WidgetPreviewContext(family: family)
        )
        .preferredColorScheme(.light)
      }

  }
}
