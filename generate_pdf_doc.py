import os
import base64
import subprocess

def get_base64_img(file_path):
    if not os.path.exists(file_path):
        print(f"Warning: file not found: {file_path}")
        return ""
    with open(file_path, "rb") as f:
        data = f.read()
    return "data:image/png;base64," + base64.b64encode(data).decode("utf-8")

img_dir = r"F:\Tech-Folder\MyApplication\.artifacts\11031efc-4398-4f03-9894-b34dcff2844b"

images = {
    "splash": get_base64_img(os.path.join(img_dir, "splash_screen.png")),
    "onboarding": get_base64_img(os.path.join(img_dir, "onboarding_screen.png")),
    "feed": get_base64_img(os.path.join(img_dir, "feed_screen.png")),
    "explore": get_base64_img(os.path.join(img_dir, "explore_screen.png")),
    "trending": get_base64_img(os.path.join(img_dir, "trending_screen.png")),
    "saved": get_base64_img(os.path.join(img_dir, "saved_screen.png")),
    "recap": get_base64_img(os.path.join(img_dir, "recap_screen.png")),
    "settings": get_base64_img(os.path.join(img_dir, "settings_screen.png")),
    "brief": get_base64_img(os.path.join(img_dir, "brief_view.png")),
    "full": get_base64_img(os.path.join(img_dir, "full_view.png")),
    "share": get_base64_img(os.path.join(img_dir, "share_dialog.png")),
}

html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tattle Application - Screen Architecture & Functionality Documentation</title>
    <style>
        @page {{
            size: A4 portrait;
            margin: 15mm;
        }}
        body {{
            font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif;
            color: #1f2937;
            background-color: #ffffff;
            line-height: 1.5;
            margin: 0;
            padding: 0;
        }}
        .cover {{
            text-align: center;
            padding: 40px 20px 20px 20px;
            border-bottom: 3px solid #e53935;
            margin-bottom: 30px;
            page-break-after: always;
        }}
        .logo-title {{
            font-size: 42px;
            font-weight: 800;
            color: #e53935;
            letter-spacing: -1px;
            margin-bottom: 5px;
        }}
        .cover-subtitle {{
            font-size: 20px;
            color: #4b5563;
            font-weight: 500;
            margin-bottom: 25px;
        }}
        .meta-box {{
            display: inline-block;
            background-color: #f9fafb;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 15px 30px;
            text-align: left;
            font-size: 14px;
            color: #374151;
        }}
        .section-title {{
            font-size: 22px;
            font-weight: 700;
            color: #111827;
            border-bottom: 2px solid #f3f4f6;
            padding-bottom: 8px;
            margin-top: 30px;
            margin-bottom: 20px;
        }}
        .flow-container {{
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 30px;
        }}
        .flow-step {{
            background: #ffffff;
            border-left: 4px solid #e53935;
            padding: 12px 18px;
            margin-bottom: 12px;
            border-radius: 0 8px 8px 0;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }}
        .flow-step-title {{
            font-weight: 700;
            color: #0f172a;
            font-size: 15px;
        }}
        .flow-step-desc {{
            font-size: 13px;
            color: #64748b;
        }}
        .screen-card {{
            page-break-inside: avoid;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 35px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
        }}
        .screen-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #f1f5f9;
            padding-bottom: 10px;
            margin-bottom: 15px;
        }}
        .screen-name {{
            font-size: 20px;
            font-weight: 700;
            color: #0f172a;
        }}
        .screen-badge {{
            background: #fee2e2;
            color: #dc2626;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }}
        .screen-body {{
            display: flex;
            gap: 25px;
            align-items: flex-start;
        }}
        .screen-img-wrapper {{
            flex: 0 0 220px;
            text-align: center;
        }}
        .screen-img {{
            width: 220px;
            height: auto;
            border-radius: 12px;
            border: 2px solid #cbd5e1;
            box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
        }}
        .screen-details {{
            flex: 1;
        }}
        .detail-block {{
            margin-bottom: 12px;
        }}
        .detail-label {{
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #e53935;
            margin-bottom: 4px;
        }}
        .detail-text {{
            font-size: 14px;
            color: #334155;
            margin: 0;
        }}
        ul.feature-list {{
            margin: 4px 0 0 0;
            padding-left: 20px;
            font-size: 13.5px;
            color: #334155;
        }}
        ul.feature-list li {{
            margin-bottom: 4px;
        }}
        .connections-box {{
            background: #f8fafc;
            border-left: 3px solid #3b82f6;
            padding: 8px 12px;
            border-radius: 0 6px 6px 0;
            font-size: 13px;
            color: #1e293b;
            margin-top: 8px;
        }}
        .page-break {{
            page-break-after: always;
        }}
    </style>
</head>
<body>

    <div class="cover">
        <div class="logo-title">Tattle!</div>
        <div class="cover-subtitle">Complete Application Screen & Architecture Documentation</div>
        <div class="meta-box">
            <strong>Framework:</strong> Kotlin Multiplatform (KMP) & Compose Multiplatform<br>
            <strong>Target Platforms:</strong> Android, Desktop (JVM), Web (Wasm/JS), iOS<br>
            <strong>Backend Integration:</strong> Supabase (Postgrest DB & Auth), Firebase AppCheck<br>
            <strong>Total Application Screens:</strong> 11 Screens & Overlays<br>
            <strong>Generated Date:</strong> September 2026
        </div>
    </div>

    <div class="section-title">1. System Architecture & Screen Navigation Graph</div>
    <div class="flow-container">
        <p style="margin-top: 0; font-size: 14px; color: #475569;">
            The Tattle application utilizes Jetpack Navigation Compose with a centralized <code>NavHost</code> managing route transitions and dynamic state overlays.
        </p>
        <div class="flow-step">
            <div class="flow-step-title">Entry & Authentication Flow</div>
            <div class="flow-step-desc"><strong>SplashScreen</strong> (&rarr; Phone / Google / Email Auth) &rarr; Evaluates onboarding status &rarr; Navigates to <strong>OnboardingScreen</strong> (if new user) or <strong>FeedScreen</strong> (if active user session).</div>
        </div>
        <div class="flow-step">
            <div class="flow-step-title">Main Bottom Navigation Core (Persistent Navigation Bar)</div>
            <div class="flow-step-desc">Allows seamless tab switching across 4 primary core views: <strong>FeedScreen ("For You")</strong>, <strong>ExploreScreen</strong>, <strong>TrendingScreen</strong>, and <strong>SavedScreen</strong>.</div>
        </div>
        <div class="flow-step">
            <div class="flow-step-title">Secondary & Utility Screens</div>
            <div class="flow-step-desc"><strong>RecapScreen</strong> (Daily digest summary) and <strong>SettingsScreen</strong> (Preferences, theme, language, data purge).</div>
        </div>
        <div class="flow-step">
            <div class="flow-step-title">Global Modal Overlays</div>
            <div class="flow-step-desc">Overlaid dynamically on top of active screens without losing state: <strong>BriefView</strong> (15s speed summary), <strong>FullView</strong> (Full article & TTS audio reader), and <strong>ShareCardDialog</strong> (Social share dialog).</div>
        </div>
    </div>

    <div class="page-break"></div>

    <div class="section-title">2. Detailed Screen Breakdown & Screen Shots</div>

    <!-- 1. Splash Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">1. Splash & Authentication Screen</span>
            <span class="screen-badge">Route: splash</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['splash']}" alt="Splash Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Serves as the application entry point. Welcomes users with an animated logo, tagline, world map backdrop, and authentication options (Google OAuth, Phone SMS OTP, or Email sign-in).</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>World map watermark backdrop with opacity styling.</li>
                        <li>Branded Tattle logo with drop-shadow effects.</li>
                        <li>Social Authentication buttons: Google Sign-In, Phone Verification, Email.</li>
                        <li>Language selector and terms of service footer notice.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Navigates to <code>OnboardingScreen</code> if user is not onboarded, or directly to <code>FeedScreen</code> if user session is already authenticated.
                </div>
            </div>
        </div>
    </div>

    <!-- 2. Onboarding Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">2. Onboarding & Personalization Screen</span>
            <span class="screen-badge">Route: onboarding</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['onboarding']}" alt="Onboarding Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Guides new users through setting up their personal profile, DOB age validation, topic preferences, and notification settings to tailor the AI recommendation algorithm.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Step progress bar indicator.</li>
                        <li>DOB date input with 12:01 AM midnight rollover cutoff logic.</li>
                        <li>Interactive interest sector chips (AI, Startups, Gaming, Science, World News).</li>
                        <li>Notification switches for Daily Briefings, Breaking News, and Streaks.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Back button returns to <code>SplashScreen</code>. Completion saves <code>UserPreferences</code> and transitions to <code>FeedScreen</code>.
                </div>
            </div>
        </div>
    </div>

    <div class="page-break"></div>

    <!-- 3. Feed Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">3. Feed Screen ("For You")</span>
            <span class="screen-badge">Route: feed</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['feed']}" alt="Feed Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">The primary news hub of Tattle. Displays personalized card feeds categorized by user interests, interactive community surveys, streak indicators, and quick reading actions.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Horizontal topic filter tabs (For You, AI, Tech, Startups, etc.).</li>
                        <li>News Cards with publisher logo, read time, hook preview, and blur hash image placeholder.</li>
                        <li>Reading streak counter badge (# days active).</li>
                        <li>Interactive inline Survey Cards with real-time poll results.</li>
                        <li>End of session recap banner.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Tapping article card opens <code>FullView</code>; brief icon opens <code>BriefView</code>; share icon opens <code>ShareCardDialog</code>; settings gear opens <code>SettingsScreen</code>; session end button opens <code>RecapScreen</code>.
                </div>
            </div>
        </div>
    </div>

    <!-- 4. Explore Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">4. Explore & Sector Discovery Screen</span>
            <span class="screen-badge">Route: explore</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['explore']}" alt="Explore Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Allows users to discover new categories, search topics across 16+ sectors, and view top curated articles across specialized domains.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Instant search bar with topic filtering.</li>
                        <li>Visual sector grid cards with gradient backgrounds and custom icons.</li>
                        <li>Recommended topic collections and sector specific story lists.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Tapping a sector selects that tab and navigates to <code>FeedScreen</code>. Tapping an article opens <code>FullView</code>. Header button navigates to <code>SettingsScreen</code>.
                </div>
            </div>
        </div>
    </div>

    <div class="page-break"></div>

    <!-- 5. Trending Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">5. Trending Stories Screen</span>
            <span class="screen-badge">Route: trending</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['trending']}" alt="Trending Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Surfaces real-time viral technology news, breaking developments, and top-read stories ranked dynamically by community engagement scores.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Rank badges (#1, #2, #3 Viral Rank).</li>
                        <li>Breaking news indicator badges.</li>
                        <li>View counts, likes, and discussion comments counters.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Tapping any trending card opens <code>FullView</code>. Header settings icon navigates to <code>SettingsScreen</code>.
                </div>
            </div>
        </div>
    </div>

    <!-- 6. Saved Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">6. Saved Articles Screen</span>
            <span class="screen-badge">Route: saved</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['saved']}" alt="Saved Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Acts as a personal offline library containing all bookmarked articles, enabling readers to save stories for later consumption.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Search bar for filtering bookmarked items.</li>
                        <li>Bookmark removal toggle button.</li>
                        <li>Empty state placeholder with quick discovery link.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Tapping a saved card launches <code>FullView</code>. Header button navigates to <code>SettingsScreen</code>.
                </div>
            </div>
        </div>
    </div>

    <div class="page-break"></div>

    <!-- 7. Recap Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">7. Daily Recap Screen</span>
            <span class="screen-badge">Route: recap</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['recap']}" alt="Recap Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Presents an end-of-day digest summarizing total cards read, streak progress, key insights consumed today, and AI-generated summary bullet points.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Reading statistics metrics cards (Cards read, Streak days, Time saved).</li>
                        <li>Key Takeaways list consumed during the day.</li>
                        <li>"Tune Feed" preferences shortcut button.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> "Tune Feed" navigates to <code>SettingsScreen</code>. Back button returns to <code>FeedScreen</code>.
                </div>
            </div>
        </div>
    </div>

    <!-- 8. Settings Screen -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">8. Settings & Customization Screen</span>
            <span class="screen-badge">Route: settings</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['settings']}" alt="Settings Screen">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Central control panel for adjusting app preferences including Theme (Dark/Light mode), Language selection (English, Hindi), Notifications, and Data Purge options.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Dark Theme toggle switch.</li>
                        <li>Language selection dropdown picker.</li>
                        <li>Interest category chip manager.</li>
                        <li>Purge Data action button with confirmation dialog.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Back button pops backstack. Purge Data clears database/preferences and resets to <code>SplashScreen</code>.
                </div>
            </div>
        </div>
    </div>

    <div class="page-break"></div>

    <!-- 9. Brief View Overlay -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">9. Brief View Overlay</span>
            <span class="screen-badge">Overlay Component</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['brief']}" alt="Brief View">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Speed-reading modal sheet that slides up from the bottom. Gives a 15-second overview with 3 high-impact bullet points and a "Why It Matters" highlight.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Slide-up modal container with semi-transparent background overlay.</li>
                        <li>3 Bullet takeaway points with icon highlights.</li>
                        <li>"Why It Matters" highlight box.</li>
                        <li>Bookmark and Share action buttons.</li>
                        <li>"Read Full Article" expansion button.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Overlaid on top of active screen. Close button dismisses modal. "Read Full Article" launches <code>FullView</code> overlay. Share opens <code>ShareCardDialog</code>.
                </div>
            </div>
        </div>
    </div>

    <!-- 10. Full View Overlay -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">10. Full Article View Overlay</span>
            <span class="screen-badge">Overlay Component</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['full']}" alt="Full Article View">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Immersive full-screen article reader. Displays full article text, high-resolution header image, integrated Text-To-Speech (TTS) audio player, and community comments section.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Hero article image header.</li>
                        <li>Publisher attribution and publication timestamp.</li>
                        <li>Integrated Audio Reader (TTS) playback bar with Play/Pause controls.</li>
                        <li>Reader controls: Font size adjustments and Dark/Light toggle.</li>
                        <li>Tabbed sections: Full Story, Key Takeaways, Comments & Discussion.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Close button dismisses reader and returns to calling screen. Share button opens <code>ShareCardDialog</code>.
                </div>
            </div>
        </div>
    </div>

    <div class="page-break"></div>

    <!-- 11. Share Card Dialog -->
    <div class="screen-card">
        <div class="screen-header">
            <span class="screen-name">11. Share Card Dialog</span>
            <span class="screen-badge">Modal Dialog</span>
        </div>
        <div class="screen-body">
            <div class="screen-img-wrapper">
                <img class="screen-img" src="{images['share']}" alt="Share Dialog">
            </div>
            <div class="screen-details">
                <div class="detail-block">
                    <div class="detail-label">Purpose & Overview</div>
                    <p class="detail-text">Modal share card generator dialog. Formats article summaries into a visually appealing share card with QR code link for easy social media sharing.</p>
                </div>
                <div class="detail-block">
                    <div class="detail-label">Key Features & UI Elements</div>
                    <ul class="feature-list">
                        <li>Styled card preview container with Tattle branding.</li>
                        <li>Headline and key takeaway bullet snippet.</li>
                        <li>QR Code leading to article source URL.</li>
                        <li>"Share Text" and "Share Image" confirmation buttons.</li>
                    </ul>
                </div>
                <div class="connections-box">
                    <strong>Connections:</strong> Overlaid modally. Confirm share triggers native system share sheet via platform ShareUtils.
                </div>
            </div>
        </div>
    </div>

</body>
</html>
"""

html_path = r"F:\Tech-Folder\MyApplication\app_screens_doc.html"
pdf_path = r"F:\Tech-Folder\MyApplication\Tattle_Application_Screens_Documentation.pdf"

with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"HTML generated at {html_path}")

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
cmd = [
    edge_path,
    "--headless",
    "--disable-gpu",
    "--no-pdf-header-footer",
    f"--print-to-pdf={pdf_path}",
    html_path
]

print("Rendering PDF via Microsoft Edge...")
res = subprocess.run(cmd, capture_output=True, text=True)
if res.returncode == 0 and os.path.exists(pdf_path):
    size_kb = os.path.getsize(pdf_path) / 1024
    print(f"SUCCESS: PDF generated successfully at {pdf_path} ({size_kb:.1f} KB)")
else:
    print(f"Error generating PDF: {res.stderr}")
