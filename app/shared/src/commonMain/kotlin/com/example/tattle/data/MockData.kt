package com.example.tattle.data

import com.example.tattle.models.Article
import com.example.tattle.models.Survey
import com.example.tattle.models.SurveyQuestion

object MockData {
    val articles = listOf(
        Article(
            id = "1",
            category = "AI & Robotics",
            readTime = "30s read",
            headline = "Neon Exodus: City Grid 7 Faces Massive System Blackout",
            hook = "Technicians report a localized power failure in the cyber-district, affecting over 2 million residents. Grid authorities suspect a coordinated digital breach.",
            brief = "A massive power failure has plunged City Grid 7 into absolute darkness. Technicians are scrambling to isolate a rogue worm detected in the primary substation's firmware. Authorities are urging residents to rely on off-grid localized mesh cells for communication.",
            bullets = listOf(
                "2M+ residents are completely dark, with all transit and commerce lines offline.",
                "Cyber-security task forces have flagged a coordinated digital breach utilizing recycled hardware elements.",
                "Off-grid localized mesh networks are seeing record-high traffic as emergency lines choke."
            ),
            whyItMatters = "This isn't a simple infrastructure failure; it is a live-fire demonstration of modern grid warfare. If localized smart substation controls are vulnerable to recycled hardware exploits, every high-density metropolitan area is actively sitting on a ticking clock.",
            fullText = """The digital void isn't empty; it's overflowing. For the modern urban citizen, information is no longer a curated stream but a relentless flood. Yesterday's power failure in Sector 7 proves that our reliance on centralized digital systems has turned convenience into our greatest point of failure.

At precisely 22:14 local time, the core automated switches of City Grid 7 fell silent. According to lead firmware engineer Kaelen Vance, a polymorphic command loop bypassed the secondary firewalls by masquerading as a routine diagnostic packet. The attack was surgical, taking down localized distribution lines while leaving the primary generators spinning uselessly.

"What we are witnessing is the balkanization of infrastructure security," says cyber-defense analyst Alex Thorne. "When you mix modern automated smart grids with legacy components bought from secondary global markets, you create backdoor avenues that are almost impossible to audit in real-time."

For the next 6 hours, Grid 7 was forced into a state of absolute analog primitive existence. Local hackers quickly set up low-power FM relays and point-to-point Wi-Fi grids to coordinate basic logistics. This grassroots mesh response shows a resilient, decentralizing impulse among the younger demographic, who have zero trust in central utilities to keep them secure.

The incident is a stark reminder that as we accelerate into direct-to-brain interfaces and fully autonomous municipal agents, maintaining independent offline capacities is no longer just a hobbyist interest—it is a baseline survival protocol.""",
            publisher = "Tattle Reports",
            publishedAt = "2h ago",
            imageUrl = "https://picsum.photos/seed/tattle1/800/1000",
            likes = 1200,
            commentsCount = 482,
            isBreaking = true,
            views = 9500,
            language = "English"
        ),
        Article(
            id = "2",
            category = "Tech",
            readTime = "6 min read",
            headline = "The Silent Rise of Neural-Mesh Networks in Urban Grids",
            hook = "While telecom giants push expensive premium licenses, a grassroots community of hackers is weaving a completely free, sovereign internet using cheap radio nodes.",
            brief = "Decentralized mesh networks are silently spreading across major global metropolises, offering a secure alternative to corporate-monitored internet pathways.",
            bullets = listOf(
                "Solar nodes form a self-healing, peer-to-peer data web.",
                "Data is split across multiple pathways, making surveillance impossible.",
                "Over 45,000 active devices are online across Tokyo and Berlin."
            ),
            whyItMatters = "Neural-mesh networks represent a technological reclamation of sovereignty and peer-to-peer resilience.",
            fullText = """While the world's largest telecommunications conglomerates argue over spectral bandwidth auctions, a decentralized network revolution is silently unfolding beneath the radar. Across Tokyo, Berlin, and San Francisco, open-source mesh relays are stitching together a parallel communications infrastructure.

Operating on license-free 915MHz and 2.4GHz frequencies, these low-power solar nodes form a resilient lattice. Even if central ISP backbones are severed or throttled, mesh packets dynamically reroute around dead zones in milliseconds.

"This is fundamentally un-censorable infrastructure," says lead developer Mira Chen. "You cannot turn off an internet that lives on thousands of rooftop solar bricks."

As privacy concerns escalate worldwide, community-managed mesh grids offer citizens an uncompromised medium for real-time local discourse and emergency response.""",
            publisher = "Tattle Reports",
            publishedAt = "3h ago",
            imageUrl = "https://picsum.photos/seed/tattle2/800/1000",
            likes = 1540,
            commentsCount = 312,
            isBreaking = false,
            views = 8200,
            language = "English"
        ),
        Article(
            id = "3",
            category = "Pop Culture",
            readTime = "4 min read",
            headline = "Virtual Collective: The Underground Scene of Digital Nomads",
            hook = "Bypassing conventional residency laws, a new nomadic generation is establishing virtual micronations governed by smart contracts.",
            brief = "A new wave of decentralized digital nomads are abandoning geographical citizenship in favor of autonomous online collectives.",
            bullets = listOf(
                "Members pool income into shared smart contracts for global housing hubs.",
                "Collective members share physical living spaces and travel dynamically.",
                "Highly skilled digital labor relocates out of reach of traditional state bureaucracies."
            ),
            whyItMatters = "Gen Z and millennial remote workers are actively inventing a post-national, borderless way of living and working.",
            fullText = """The traditional concept of a nation-state is anchored in physical soil, but the rising generation belongs to the cloud. Over the past three years, cloud-first collectives have purchased real estate in Portugal, Bali, and Costa Rica.

Instead of traditional mortgages or leases, governance is handled via on-chain quadratic voting. Members contribute a fixed percentage of remote earnings into a shared treasury, which finances coliving compounds, starlink connectivity, and organic farming collectives.

Sociologists term this 'sovereign nomadism'—a paradigm shift where community ties and legal identity reside on immutable decentralized ledgers rather than passport offices.""",
            publisher = "Creator Lab",
            publishedAt = "5h ago",
            imageUrl = "https://picsum.photos/seed/tattle3/800/1000",
            likes = 980,
            commentsCount = 198,
            isBreaking = false,
            views = 6100,
            language = "English"
        ),
        Article(
            id = "4",
            category = "World News",
            readTime = "30s read",
            headline = "Decentralized Voting: Global Trial Runs Show Surprising Youth Turnout",
            hook = "A modern district-level voting trial using ledger-verified tokens recorded over 94% participation among youth voters.",
            brief = "A pilot program for localized cryptographically secure voting has successfully concluded across 12 municipal regions.",
            bullets = listOf(
                "Youth voter participation spiked to 94.2%, up from 22% in traditional elections.",
                "Results were compiled and verified within 4 minutes with zero tampering flagged.",
                "Public support is massive despite skepticism from traditional political parties."
            ),
            whyItMatters = "When voting is friction-free, instantaneous, and verifiably transparent, political engagement undergoes a radical transformation.",
            fullText = """For decades, low youth voter turnout was treated as an inevitable civic apathy. However, pilot trials conducted in Zurich, Seoul, and Austin have thoroughly debunked that assumption.

By replacing paper ballots and inconvenient polling locations with zero-knowledge mobile voter passes, voting took less than 15 seconds per citizen. The cryptographic protocol allowed voters to verify their ballot was counted correctly without revealing their individual choice.

Municipal leaders are now drafting legislation to expand tokenized voting to municipal budget allocations next quarter.""",
            publisher = "CivicTech Monthly",
            publishedAt = "8h ago",
            imageUrl = "https://picsum.photos/seed/tattle4/800/1000",
            likes = 2120,
            commentsCount = 654,
            isBreaking = false,
            views = 11400,
            language = "English"
        ),
        Article(
            id = "5",
            category = "AI & Robotics",
            readTime = "5 min read",
            headline = "Global Semiconductor Shortage Hits Critical Infrastructure",
            hook = "The semiconductor supply chain crisis has escalated, impacting metropolitan energy grids and autonomous transit networks.",
            brief = "High-density urban energy grids are experiencing implementation lags due to microcontroller chip delays worldwide.",
            bullets = listOf(
                "Smart grid upgrades stall until Q3 2025 across Europe and North America.",
                "Secondary market pricing for legacy controllers spiked 400% in weeks.",
                "Recycled chips increase the attack surface for hardware exploits."
            ),
            whyItMatters = "When fundamental infrastructure cannot access hardware, societal stability becomes an unpredictable variable.",
            fullText = """The global silicon chokehold has reached a critical juncture. What began as consumer electronics delays has spilled over into essential public utilities. Power distribution grids, clean water processing facilities, and automated light rail lines are facing severe hardware shortfalls.

Factory backlogs for industrial-grade 28nm microcontrollers now exceed 18 months. Energy providers have been forced to rely on refurbished secondary market components, raising concerns among cybersecurity experts.

"Hardware authenticity is the foundation of trust," warns supply chain researcher Dr. Elena Rostova. "When utilities buy unverified surplus chips, backdoor vulnerabilities become almost unavoidable."""",
            publisher = "Tattle Reports",
            publishedAt = "12h ago",
            imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&q=80",
            likes = 3410,
            commentsCount = 912,
            isBreaking = true,
            views = 14200,
            language = "English"
        ),
        Article(
            id = "6",
            category = "Science & Space",
            readTime = "4 min read",
            headline = "Lunar Outpost Alpha Reports Stable Oxygen Harvest from Regolith",
            hook = "Lunar surface reactors successfully extract breathable oxygen directly from moon soil, paving the way for permanent human habitation.",
            brief = "Scientists at Lunar Outpost Alpha have achieved a continuous 90-day closed-loop oxygen synthesis milestone.",
            bullets = listOf(
                "Thermovacuum reactors process 10kg of regolith per hour into pure oxygen.",
                "Byproduct metallic titanium and silicon are harvested for 3D construction.",
                "First commercial lunar supply mission scheduled for late 2026."
            ),
            whyItMatters = "In-situ resource utilization eliminates the exorbitant cost of launching life support consumables from Earth.",
            fullText = """Humanity's permanent presence on the Moon just took a giant leap forward. The International Lunar Research Station has confirmed that molten salt electrolysis units have been continuously generating oxygen from regolith for three full months.

The breakthrough removes the primary bottleneck for off-world bases: the weight penalty of hauling oxygen rockets out of Earth's gravity well.

With breathable air secured on-site, lunar crews can now focus on expanding subterranean habitat tubes and assembling deep space transport vessels in lunar orbit.""",
            publisher = "AstroScience Journal",
            publishedAt = "1d ago",
            imageUrl = "https://picsum.photos/seed/tattle6/800/1000",
            likes = 1890,
            commentsCount = 340,
            isBreaking = false,
            views = 7800,
            language = "English"
        ),
        Article(
            id = "7",
            category = "Business & Money",
            readTime = "3 min read",
            headline = "Autonomous Algorithmic Trading Hits 60% of Global Capital Markets",
            hook = "AI agents now execute the majority of high-frequency trades, reshaping financial liquidity and market volatility.",
            brief = "Autonomous neural models are making multi-billion-dollar allocation decisions in microseconds across global exchanges.",
            bullets = listOf(
                "AI-driven trades account for $4.2 Trillion in daily volume.",
                "Human market makers fall below 15% total order flow participation.",
                "Regulators mandate real-time circuit breakers for autonomous agent clusters."
            ),
            whyItMatters = "Financial markets are evolving into automated machine-to-machine ecosystems where human reaction times are obsolete.",
            fullText = """Wall Street's trading floors were already quiet, but today they are completely automated. Autonomous portfolio algorithms trained on multi-modal economic data now control over 60% of all public equity and currency trades.

These agents analyze central bank statements, satellite imagery of shipping ports, and consumer transaction feeds in real-time, executing rebalancing moves faster than light can travel across transatlantic fiber cables.

While market efficiency has reached unprecedented levels, central bankers warn that machine herd behavior could trigger flash crashes if models share correlated bias loops.""",
            publisher = "FinTech Dispatch",
            publishedAt = "4h ago",
            imageUrl = "https://picsum.photos/seed/tattle7/800/1000",
            likes = 2450,
            commentsCount = 512,
            isBreaking = false,
            views = 10300,
            language = "English"
        ),
        Article(
            id = "8",
            category = "Climate & Environment",
            readTime = "4 min read",
            headline = "Next-Gen Perovskite Solar Cells Achieve 35% Efficiency Record",
            hook = "Tandem solar technology shatters previous limits, paving the way for ultra-cheap clean energy across urban facades.",
            brief = "Laboratory breakthroughs in flexible tandem perovskite cells promise to double solar output per square meter.",
            bullets = listOf(
                "Efficiency jumps from 22% to 35.4% in certified lab tests.",
                "Flexible thin-film design allows application directly onto skyscraper glass.",
                "Commercial manufacturing scaled for mass distribution by early 2026."
            ),
            whyItMatters = "Doubling solar efficiency converts every window and rooftop into a high-yield power plant.",
            fullText = """Solar energy adoption is about to experience an exponential curve. Engineers at the Renewable Energy Institute have solved the long-standing stability challenge of perovskite-silicon tandem cells.

Unlike rigid silicon panels, these thin films can be sprayed directly onto architectural glass, vehicle roofs, and smart clothing fabrics.

The breakthrough reduces the payback period for commercial solar installations to under two years, accelerating the phase-out of fossil fuel peaking plants.""",
            publisher = "EcoTech World",
            publishedAt = "6h ago",
            imageUrl = "https://picsum.photos/seed/tattle8/800/1000",
            likes = 1320,
            commentsCount = 210,
            isBreaking = false,
            views = 5400,
            language = "English"
        ),
        Article(
            id = "9",
            category = "Gaming",
            readTime = "5 min read",
            headline = "Generative Neural Engines Enable Infinite-World Indie Game Devs",
            hook = "Solo game creators are releasing photorealistic, procedural open-world universes powered by localized neural renderers.",
            brief = "AI game generation engines allow small 2-person studios to compete directly with AAA budget blockbusters.",
            bullets = listOf(
                "Real-time procedural physics and NPC dialogue synthesized on consumer GPUs.",
                "Indie titles top global download charts with zero publisher backing.",
                "Traditional $200M game budgets face massive market disruption."
            ),
            whyItMatters = "Democratized game development tools are shifting creative power back to independent artists.",
            fullText = """The gaming industry is undergoing its biggest structural shift since the 3D graphics revolution. Generative asset pipelines and neural behavior models mean a duo working from a garage can craft sprawling, living worlds that rival multi-hundred-million-dollar AAA releases.

NPCs respond dynamically with persistent memory, full voice acting, and contextual decision-making. Players no longer follow scripted paths; every playthrough generates a completely unique narrative arc.

"We are entering the golden age of interactive storytelling," says indie creator Liam Vance. "The friction between imagination and code has vanished."""",
            publisher = "GameDev Weekly",
            publishedAt = "7h ago",
            imageUrl = "https://picsum.photos/seed/tattle9/800/1000",
            likes = 2100,
            commentsCount = 430,
            isBreaking = false,
            views = 8900,
            language = "English"
        ),
        Article(
            id = "10",
            category = "Entertainment",
            readTime = "4 min read",
            headline = "Decentralized Streaming Protocols Disrupt Legacy Hollywood Model",
            hook = "Peer-to-peer video streaming networks allow filmmakers to monetize directly with audiences without studio middlemen.",
            brief = "Independent filmmakers are funding, distributing, and monetizing feature films directly through decentralized content nodes.",
            bullets = listOf(
                "Filmmakers retain 95% of box office and streaming revenues.",
                "Tokenized micro-patronage allows fans to earn passive royalties.",
                "Legacy streaming platforms see subscriber drops as community hubs grow."
            ),
            whyItMatters = "Direct creator-to-audience rails eliminate studio gatekeepers and corporate censorship.",
            fullText = """Hollywood studio executives are watching their distribution moat dissolve. Decentralized streaming protocols leveraging WebTorrent and smart contract royalties are enabling filmmakers to premiere films globally with zero middleman take-rates.

Audience members stake tokens to stream 4K content directly from decentralized node operators, while smart contracts automatically split revenue between actors, directors, and early backers instantly.

The model ensures niche art films and daring documentaries find immediate financial viability.""",
            publisher = "Cinema Today",
            publishedAt = "10h ago",
            imageUrl = "https://picsum.photos/seed/tattle10/800/1000",
            likes = 1150,
            commentsCount = 180,
            isBreaking = false,
            views = 4700,
            language = "English"
        ),
        Article(
            id = "11",
            category = "Startups",
            readTime = "3 min read",
            headline = "Micro-SaaS Renaissance: Solo Founders Leverage Local LLM Fleets",
            hook = "One-person companies are hitting $1M ARR in record time by automating operations with specialized AI agents.",
            brief = "Bootstrapped solo founders are outcompeting 50-person venture-backed teams using autonomous agentic workflows.",
            bullets = listOf(
                "Average time to profitability for solo startups drops to under 30 days.",
                "Local open-weights LLMs handle customer support, QA, and billing autonomously.",
                "Venture capital funds shift focus toward lean single-operator portfolios."
            ),
            whyItMatters = "Capital efficiency is reaching extreme levels as software creation and operations become friction-free.",
            fullText = """The era of massive venture capital burn rates may be coming to an end. A growing movement of 'Micro-SaaS' founders are building high-margin software products with zero employees.

By deploying fleets of open-source AI agents running on private cloud servers, solo developers handle code maintenance, customer success, marketing campaigns, and compliance.

Investors are taking notice: capital efficiency and cash flow are once again king in the tech ecosystem.""",
            publisher = "Founder Circle",
            publishedAt = "1d ago",
            imageUrl = "https://picsum.photos/seed/tattle11/800/1000",
            likes = 1980,
            commentsCount = 310,
            isBreaking = false,
            views = 9100,
            language = "English"
        ),
        Article(
            id = "12",
            category = "Healthcare",
            readTime = "5 min read",
            headline = "Personalized mRNA Therapies Enter Phase III Trial for Pancreatic Cancer",
            hook = "Customized cancer vaccines engineered in 48 hours show unprecedented remissions in clinical trials.",
            brief = "Next-generation genomic sequencing enables rapid synthesis of individualized mRNA treatments targeting tumor mutations.",
            bullets = listOf(
                "88% disease-free survival rate recorded across 500 trial participants.",
                "Vaccines are custom-synthesized within two days of biopsy extraction.",
                "FDA grants accelerated breakthrough designation for general oncology deployment."
            ),
            whyItMatters = "Oncology is transitioning from generalized chemotherapy to surgical, patient-specific immunotherapy.",
            fullText = """In what oncologists are calling a landmark medical triumph, personalized mRNA cancer vaccines have achieved unprecedented efficacy in Phase III clinical trials.

By sequencing a patient's tumor genome upon biopsy, AI algorithms identify unique neoantigens and generate a tailored mRNA strand within 48 hours.

When administered, the patient's immune system trains specifically on those tumor markers, eradicating micro-metastases without damaging healthy organ tissue.""",
            publisher = "BioHealth Journal",
            publishedAt = "2h ago",
            imageUrl = "https://picsum.photos/seed/tattle12/800/1000",
            likes = 3100,
            commentsCount = 680,
            isBreaking = true,
            views = 12500,
            language = "English"
        ),
        Article(
            id = "13",
            category = "Education",
            readTime = "4 min read",
            headline = "Adaptive AI Tutors Bridge Learning Gaps Across 50 Nations",
            hook = "Open-access personalized AI mentors provide 1-on-1 tutoring in 100+ native languages to millions of students.",
            brief = "Global literacy and STEM proficiency metrics surge as free adaptive AI tutors reach underfunded rural classrooms.",
            bullets = listOf(
                "Student math and reading mastery improves by 2.5 grade levels in 6 months.",
                "Offline voice-based models run on low-cost $30 tablet devices.",
                "UN Educational Task Force endorses AI-assisted personalized learning standards."
            ),
            whyItMatters = "Universal access to high-quality 1-on-1 education is democratizing human potential worldwide.",
            fullText = """Quality education was long constrained by geography and teacher-to-student ratios. Today, open-source adaptive learning models are turning smartphone and tablet screens into world-class private tutors.

The AI adjusts instantly to each child's learning speed, using localized cultural metaphors, interactive games, and patient real-time feedback.

From rural villages in Kenya to public schools in Detroit, educational inequality is receiving a technological solution.""",
            publisher = "EduTech Horizon",
            publishedAt = "14h ago",
            imageUrl = "https://picsum.photos/seed/tattle13/800/1000",
            likes = 1420,
            commentsCount = 220,
            isBreaking = false,
            views = 6300,
            language = "English"
        ),
        Article(
            id = "14",
            category = "Lifestyle",
            readTime = "3 min read",
            headline = "Urban Vertical Micro-Farming Transforms Metropolitan Food Security",
            hook = "Modular hydroponic skyscrapers provide hyper-local organic produce with 95% less water consumption.",
            brief = "Metropolitan centers are converting abandoned industrial warehouses into high-tech vertical agricultural hubs.",
            bullets = listOf(
                "Fresh produce travels less than 2 miles from harvest to dinner plate.",
                "Zero synthetic pesticides used; powered entirely by local solar energy.",
                "Cities cut agricultural supply chain emissions by 80%."
            ),
            whyItMatters = "Hyper-local urban food production buffers cities against climate disruptions and supply chain shocks.",
            fullText = """The farm of the future doesn't require acres of soil or tractor diesel. In cities like Chicago, Singapore, and Amsterdam, multi-story automated hydroponic facilities are harvesting fresh leafy greens year-round.

Precision LED light spectrums and closed-loop nutrient circulation allow plants to grow 50% faster while using a fraction of traditional water volume.

Urban residents enjoy farm-to-table freshness picked hours before consumption, eliminating food waste and long-haul transport.""",
            publisher = "Urban Living",
            publishedAt = "18h ago",
            imageUrl = "https://picsum.photos/seed/tattle14/800/1000",
            likes = 990,
            commentsCount = 145,
            isBreaking = false,
            views = 5100,
            language = "English"
        ),
        Article(
            id = "15",
            category = "Sports",
            readTime = "4 min read",
            headline = "AI-Powered Motion Analytics Redefining Elite Athletic Training",
            hook = "Computer vision sensor arrays prevent athletic injuries and optimize biometric performance in real-time.",
            brief = "Professional sports teams adopt biomechanical AI monitoring to extend athlete career longevity and peak output.",
            bullets = listOf(
                "Muscle strain risk flagged 48 hours before physical symptoms occur.",
                "Real-time game tactical adjustments generated by predictive models.",
                "Injury recovery timelines reduced by 40% with smart feedback loops."
            ),
            whyItMatters = "Biomechanical precision prevents career-ending injuries and elevates human physical potential.",
            fullText = """High-performance sports training has entered the era of computational biology. High-frame-rate cameras and wearable micro-sensors track thousands of skeletal movement data points per second.

AI engines analyze joint angles, force vectors, and fatigue patterns, warning athletic trainers before a micro-tear becomes a debilitating injury.

Coaches receive real-time tactical adjustments on sideline tablets, optimizing player rotations based on live biometric load.""",
            publisher = "Sports Tech Daily",
            publishedAt = "9h ago",
            imageUrl = "https://picsum.photos/seed/tattle15/800/1000",
            likes = 1650,
            commentsCount = 275,
            isBreaking = false,
            views = 7200,
            language = "English"
        ),
        Article(
            id = "16",
            category = "Creator Economy",
            readTime = "3 min read",
            headline = "Tokenized Royalty Smart Contracts Empower Independent Musicians",
            hook = "Artists raise recording capital by selling micro-shares of future streaming revenue directly to fans.",
            brief = "Decentralized fan-funding protocols allow indie musicians to remain completely independent while funding tours.",
            bullets = listOf(
                "Fans receive automatic monthly payout deposits directly to crypto wallets.",
                "Indie tracks reach top global Spotify charts without record label contracts.",
                "Musicians retain full ownership of master recordings."
            ),
            whyItMatters = "Direct fan ownership creates aligned incentives and financial independence for creative artists.",
            fullText = """Record label advance contracts with predatory terms are becoming obsolete. Independent musicians are turning to fan tokenization to fund albums and global tours.

By offering 20% of future master royalties as fractional smart contract tokens, artists raise capital directly from their most dedicated supporters in hours.

Every time a track streams on Spotify, Apple Music, or TikTok, smart contracts disburse royalties automatically to token holders worldwide.""",
            publisher = "MusicX Insider",
            publishedAt = "11h ago",
            imageUrl = "https://picsum.photos/seed/tattle16/800/1000",
            likes = 1280,
            commentsCount = 195,
            isBreaking = false,
            views = 5800,
            language = "English"
        ),
        Article(
            id = "17",
            category = "Geopolitics",
            readTime = "5 min read",
            headline = "Global Treaty Drafted to Govern Autonomous Defense Systems",
            hook = "50 nations gather in Geneva to establish binding human-in-the-loop protocols for military AI.",
            brief = "International diplomats negotiate historic frameworks regulating autonomous drone swarms and cyber weapons.",
            bullets = listOf(
                "Mandatory human authorization required for all lethal force deployments.",
                "Shared verification protocols established to prevent accidental escalation.",
                "Tech industry leaders sign pledge banning unmonitored autonomous targeting."
            ),
            whyItMatters = "Establishing international guardrails on automated warfare is critical for long-term global stability.",
            fullText = """Representatives from 50 nations have convened at the United Nations Geneva headquarters for emergency non-proliferation talks focused on autonomous weapons systems.

The draft treaty establishes strict international standards requiring verifiable human operator intervention before any kinetic action is taken by autonomous systems.

Delegates stressed that transparent international verification mechanisms are essential to prevent miscalculation in an era of hypersonic AI defense networks.""",
            publisher = "Global Affairs Review",
            publishedAt = "3h ago",
            imageUrl = "https://picsum.photos/seed/tattle17/800/1000",
            likes = 2890,
            commentsCount = 610,
            isBreaking = true,
            views = 13100,
            language = "English"
        )
    )

    val surveys = listOf(
        Survey(
            id = "s1",
            title = "Tattle Reader Checkpoint",
            questions = listOf(
                SurveyQuestion(
                    id = "q1",
                    question = "Which tech domain interests you the most for daily reading?",
                    options = listOf(
                        "AI & Generative LLMs",
                        "Clean Energy & Climate Tech",
                        "Cybersecurity & Mesh Networks",
                        "Space Exploration & Biotech"
                    )
                ),
                SurveyQuestion(
                    id = "q2",
                    question = "How often do you prefer receiving breaking news updates?",
                    options = listOf(
                        "Instant Breaking Alerts",
                        "Twice Daily Briefings",
                        "Evening Summary Only",
                        "Weekend Deep Dives"
                    )
                ),
                SurveyQuestion(
                    id = "q3",
                    question = "What reading format do you find most valuable?",
                    options = listOf(
                        "30-Second Executive Briefs",
                        "Bullet Key Takeaways",
                        "In-Depth Analytical Reports",
                        "Interactive Infographics"
                    )
                ),
                SurveyQuestion(
                    id = "q4",
                    question = "How would you rate your Tattle reading experience so far?",
                    options = listOf(
                        "⭐⭐⭐⭐⭐ Exceptional",
                        "Very Good",
                        "Good",
                        "Needs Improvement"
                    )
                )
            )
        )
    )

    fun getArticlesBySector(sector: String): List<Article> {
        val clean = sector.trim().lowercase()
        if (clean == "all" || clean == "for_you" || clean == "for you") {
            return articles
        }
        val matched = articles.filter { article ->
            val cat = article.category.lowercase()
            cat.contains(clean) || clean.contains(cat) || isSectorMatch(clean, cat)
        }
        return if (matched.isNotEmpty()) matched else articles.shuffled()
    }

    private fun isSectorMatch(sector: String, category: String): Boolean {
        return when (sector) {
            "ai" -> category.contains("ai") || category.contains("tech") || category.contains("robotics")
            "tech" -> category.contains("tech") || category.contains("ai") || category.contains("gaming")
            "world news", "world_news" -> category.contains("world") || category.contains("politics") || category.contains("geopolitics")
            "money and business", "money_and_business", "business" -> category.contains("business") || category.contains("money") || category.contains("finance") || category.contains("startups")
            "science and space", "science_and_space", "science" -> category.contains("science") || category.contains("space") || category.contains("tech")
            "climate and environment", "climate_and_environment" -> category.contains("climate") || category.contains("environment")
            "gaming" -> category.contains("gaming") || category.contains("tech")
            "media and entertainment", "entertainment" -> category.contains("entertainment") || category.contains("pop")
            "pop culture", "pop_culture" -> category.contains("pop") || category.contains("creator") || category.contains("entertainment")
            "geopolitics" -> category.contains("geopolitics") || category.contains("politics") || category.contains("world")
            "sports" -> category.contains("sports")
            "startups" -> category.contains("startups") || category.contains("business")
            "healthcare" -> category.contains("healthcare") || category.contains("health")
            "education" -> category.contains("education")
            "lifestyle" -> category.contains("lifestyle")
            "creator economy", "creator_economy" -> category.contains("creator") || category.contains("pop")
            else -> false
        }
    }

    fun getTrendingArticles(): List<Article> {
        return articles.sortedByDescending { it.views + it.likes * 2 }
    }
}
