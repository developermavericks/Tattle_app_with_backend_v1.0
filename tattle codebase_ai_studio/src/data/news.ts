/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { Article, Survey } from "../types";

export const mockArticles: Article[] = [
  {
    id: "1",
    category: "Tech & AI",
    readTime: "30s read",
    headline: "Neon Exodus: City Grid 7 Faces Massive System Blackout",
    hook: "Technicians report a localized power failure in the cyber-district, affecting over 2 million residents. Grid authorities suspect a coordinated digital breach.",
    brief: "A massive power failure has plunged City Grid 7 into absolute darkness. Technicians are scrambling to isolate a rogue worm detected in the primary substation's firmware. Authorities are urging residents to rely on off-grid localized mesh cells for communication.",
    bullets: [
      "2M+ residents are completely dark, with all transit and commerce lines offline.",
      "Cyber-security task forces have flagged a coordinated digital breach utilizing recycled hardware elements.",
      "Off-grid localized mesh networks are seeing record-high traffic as emergency lines choke."
    ],
    whyItMatters: "This isn't a simple infrastructure failure; it is a live-fire demonstration of modern grid warfare. If localized smart substation controls are vulnerable to recycled hardware exploits, every high-density metropolitan area is actively sitting on a ticking clock.",
    fullText: `The digital void isn't empty; it's overflowing. For the modern urban citizen, information is no longer a curated stream but a relentless flood. Yesterday's power failure in Sector 7 proves that our reliance on centralized digital systems has turned convenience into our greatest point of failure.

At precisely 22:14 local time, the core automated switches of City Grid 7 fell silent. According to lead firmware engineer Kaelen Vance, a polymorphic command loop bypassed the secondary firewalls by masquerading as a routine diagnostic packet. The attack was surgical, taking down localized distribution lines while leaving the primary generators spinning uselessly.

"What we are witnessing is the balkanization of infrastructure security," says cyber-defense analyst Alex Thorne. "When you mix modern automated smart grids with legacy components bought from secondary global markets, you create backdoor avenues that are almost impossible to audit in real-time."

For the next 6 hours, Grid 7 was forced into a state of absolute analog primitive existence. Local hackers quickly set up low-power FM relays and point-to-point Wi-Fi grids to coordinate basic logistics. This grassroots mesh response shows a resilient, decentralizing impulse among the younger demographic, who have zero trust in central utilities to keep them secure.

The incident is a stark reminder that as we accelerate into direct-to-brain interfaces and fully autonomous municipal agents, maintaining independent offline capacities is no longer just a hobbyist interest—it is a baseline survival protocol.`,
    publisher: "Tattle Reports",
    publishedAt: "2h ago",
    imageUrl: "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600&auto=format&fit=crop",
    likes: 1200,
    commentsCount: 482,
    isBreaking: true,
    views: 4500
  },
  {
    id: "2",
    category: "Future Tech",
    readTime: "6 min read",
    headline: "The Silent Rise of Neural-Mesh Networks in Urban Grids",
    hook: "While telecom giants push expensive premium licenses, a grassroots community of hackers is weaving a completely free, sovereign internet using cheap radio nodes.",
    brief: "Decentralized mesh networks are silently spreading across major global metropolises, offering a secure alternative to corporate-monitored internet pathways. Running on solar-powered radio links, these nodes bypass central internet service providers completely.",
    bullets: [
      "Solar nodes installed on fire escapes and roofs form a self-healing, peer-to-peer data web.",
      "Data is heavily encrypted and split across multiple pathways, making surveillance virtually impossible.",
      "Over 45,000 active devices are now online across Tokyo and Berlin hub networks."
    ],
    whyItMatters: "For Gen Z, who have lived their entire lives under corporate surveillance and algorithmic curation, neural-mesh networks represent the first genuine return to the democratic, wild-west ethos of the early web. It is a technological reclamation of sovereignty.",
    fullText: `While the world's largest telecommunications conglomerates argue over bandwidth auctions and data collection permissions, a quiet revolution is taking place on the rooftops of our cities.

They call it the Mesh. Built using custom-flashed, low-power microcontrollers costing less than a standard meal, these devices connect to each other dynamically. If Node A wants to send a message to Node D, it hops through Nodes B and C without ever touching a commercial server or fiber optic trunk.

"I don't want a provider looking at my query history to serve me carbonated beverage ads," says Yuki, a 19-year-old developer in Akihabara who manages a node cluster. Yuki's neighborhood network hosts chat servers, localized encyclopedia mirrors, and a peer-to-peer barter market—all running completely independent of the global grid.

This trend is highly localized but scaling rapidly. Because the protocols are open-source and self-healing, the network grows more robust as more people join. If a single building goes down, the traffic automatically routes around the hole.

The silent expansion of the Mesh represents a deep-seated cultural pivot: we are moving past the expectation of centralized safety, choosing instead to build the digital floorboards we walk on ourselves.`,
    publisher: "Tattle Reports",
    publishedAt: "3h ago",
    imageUrl: "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=600&auto=format&fit=crop",
    likes: 1540,
    commentsCount: 312,
    isBreaking: false,
    views: 3200
  },
  {
    id: "3",
    category: "Pop Culture",
    readTime: "4 min read",
    headline: "Virtual Collective: The underground scene of digital nomads",
    hook: "Bypassing conventional residency laws, a new nomadic generation is establishing fully functional virtual micronations in server spaces.",
    brief: "A new wave of decentralized digital nomads are abandoning geographical citizenship in favor of virtual collective states. These 'cloud communities' feature independent currency pools, mutual aid networks, and distinct cultural identities.",
    bullets: [
      "Members pool income into shared smart contracts, providing universal basic security for all participants.",
      "Collective members share living spaces, co-working facilities, and travel expenses dynamically.",
      "Traditional states are starting to sweat as highly skilled digital labor relocates out of reach."
    ],
    whyItMatters: "This is the ultimate evolution of 'work from home.' By severing the connection between income, citizenship, and geographical boundaries, Gen Z is actively inventing a post-national way of living.",
    fullText: `The traditional concept of a 'nation' is built on dirt. The virtual collective 'Somnium' is built on latency.

Somnium began as a private chat server for freelance creative workers during the mid-2020s. Today, it functions as a highly organized virtual cooperative with over 8,000 full-time 'citizens' who live across five continents but operate under a unified financial and cultural charter.

"I pay taxes to a digital treasury, not a local bureaucracy," explains Chloe, a digital product designer currently based in a shared surf house in Costa Rica. "In return, if my client contracts dry up, the treasury automatically provides a monthly basic income pool. My safety net is global and code-enforced, not dependent on local political whims."

Somnium isn't alone. Dozens of similar collectives are popping up, organized around artistic movements, gaming leagues, or open-source engineering groups. They lease physical properties in real-world locations as temporary embassy hubs where members can rotate in and out.

As physical borders grow more friction-dense and cost-prohibitive, the appeal of a sovereign digital community will only intensify. The future of citizenship might not be defined by your passport, but by your digital repository.`,
    publisher: "Creator Lab",
    publishedAt: "5h ago",
    imageUrl: "https://images.unsplash.com/photo-1522071820081-009f0129c71c?q=80&w=600&auto=format&fit=crop",
    likes: 980,
    commentsCount: 198,
    isBreaking: false,
    views: 2100
  },
  {
    id: "4",
    category: "Politics",
    readTime: "30s read",
    headline: "Decentralized Voting: Trial runs show surprising results",
    hook: "A modern district-level voting trial using cryptographic, ledger-verified tokens recorded over 94% participation among youth.",
    brief: "A pilot program for localized cryptographically secure voting has successfully concluded in District 9, reporting record-high turnouts and zero recorded credential exploits. By allowing voters to authenticate and cast ballots from their mobile devices, the trial solved the historic youth turnout dilemma in single stroke.",
    bullets: [
      "Youth voter participation spiked from a historical 24% to a stunning 94.2%.",
      "Results were compiled and fully verified within 4 minutes of the poll closing.",
      "Traditional party representatives are raising standard security questions, but the public support is massive."
    ],
    whyItMatters: "Gen Z is done standing in physical lines for hours to cast a paper ballot that feels like it disappears into a black hole. When you make voting as fast, secure, and intuitive as checking an app, the political landscape shifts instantaneously.",
    fullText: `For decades, political scientists have repeated the same tired mantra: 'the youth just don't show up to vote.' 

District 9's latest civic trial has officially shattered that myth. By replacing centralized polling places with direct-to-device cryptographic voting, the local election commission registered a turnout that would make national democracies green with envy.

The technology uses blind signatures and multi-party computation. To the user, it is as simple as verifying their ID via their device's secure enclave and clicking a button. To the system, it represents an unalterable, fully auditable trail that verifies every single vote without compromising user anonymity.

"It felt like buying a concert ticket, except it actually affects how my neighborhood funds its schools," says 18-year-old voter Liam. "If I can manage my entire financial life from my phone, there is zero reason why I should have to mail a piece of cardboard to have my voice heard."

While conservative lawmakers are calling for audits, independent security observers have praised the trial as the cleanest and most transparent vote in the state's modern history. The question is no longer whether device voting is ready; it is whether the current political class is ready for the wave of voters it unleashes.`,
    publisher: "CivicTech Monthly",
    publishedAt: "8h ago",
    imageUrl: "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?q=80&w=600&auto=format&fit=crop",
    likes: 2120,
    commentsCount: 654,
    isBreaking: false,
    views: 6700
  },
  {
    id: "5",
    category: "Tech & AI",
    readTime: "5 min read",
    headline: "Global Semiconductor Shortage Hits Critical Infrastructure: What's Next?",
    hook: "The ongoing semiconductor supply chain crisis has escalated to a new critical phase, directly impacting metropolitan energy grids and communications hardware.",
    brief: "The semiconductor supply chain crisis has entered an urgent phase. High-density urban energy grids and communication networks across Europe and North America are experiencing massive implementation lags due to a 14-month delivery delay on specialized microcontrollers.",
    bullets: [
      "Major smart-grid implementations in EMEA and North America are officially stalled until Q3 2025.",
      "Secondary market pricing for legacy controllers has spiked 400% as companies scramble for replacements.",
      "Security experts warn that the use of recycled chips increases the attack surface for hardware-level exploits."
    ],
    whyItMatters: "This isn't just about delayed consumer electronics anymore. When the fundamental layers of the internet and power generation can't access hardware, societal stability becomes a variable. Expect a rise in demand for low-level engineering skills.",
    fullText: `The ongoing semiconductor supply chain crisis has escalated to a new critical phase, directly impacting metropolitan energy grids and communications hardware across three continents. Manufacturing delays for specialized micro-controllers have reached a staggering 14-month lead time, forcing major infrastructure providers to delay essential upgrades. Analysts warn that the ripple effect could lead to intermittent service disruptions in high-density urban areas by mid-next year.

This isn't just about delayed consumer electronics anymore. When the fundamental layers of the internet and power generation can't access hardware, societal stability becomes a variable. 

For Gen Z, this signals a shift from "hyper-growth" to a "patchwork economy," where maintaining existing digital systems will become more lucrative than building new ones. Expect a rise in the demand for low-level engineering skills and local mesh networking expertise.`,
    publisher: "Tattle Reports",
    publishedAt: "12h ago",
    imageUrl: "https://images.unsplash.com/photo-1581092160607-ee22621dd758?q=80&w=600&auto=format&fit=crop",
    likes: 3410,
    commentsCount: 912,
    isBreaking: true,
    views: 9400
  },
  {
    id: "6",
    category: "Tech & AI",
    readTime: "4 min read",
    headline: "Neural Linkage: The Ethics of Direct-to-Brain Computing",
    hook: "As consumer neural implants pass clinical human trials, we face a critical question: how do you keep your thoughts fully private when connected to a server?",
    brief: "With neural connection implants transitioning from research labs to mainstream consumer pre-orders, the boundary between direct thoughts and digital marketing is dissolving. Technologists are warning that brain-computer interfaces lack foundational data privacy protection frameworks.",
    bullets: [
      "mainstream pre-orders for basic reading-headbands have exceeded 500,000 units in North America alone.",
      "No direct laws exist to prevent companies from aggregating subconscious gaze and focus metrics.",
      "A growing collective of bio-hackers is designing local hardware-kill switches for neural receivers."
    ],
    whyItMatters: "When an algorithm doesn't just read your click history, but tracks your immediate subconscious reaction to a visual prompt, cognitive privacy ceases to exist. We must establish neural-rights frameworks before the tech becomes default.",
    fullText: `The ultimate frontier isn't space. It's the grey matter between your ears.

Within the next 18 months, consumer-grade brain-computer interfaces (BCIs) will begin shipping to hundreds of thousands of early adopters. These are not medical devices designed to rehabilitate motor functions; they are marketed as productivity enhancers, allowing gamers to execute commands at the speed of thought and creatives to paint digital canvases without touching a stylus.

But this speed comes with an unprecedented privacy cost. 

"Your current phone can infer your mood by how fast you scroll," says neural-ethics advocate Sarah Lin. "A BCI doesn't need to guess. It reads your immediate, pre-conscious emotional response to an image before you've even formulated a verbal thought. That data is gold for advertisers, and currently, there are zero laws to stop them from harvesting it."

The development has triggered an active pushback among tech-conscious youth. Under the banner of 'cognitive liberty,' hacker groups are developing open-source signal filters that intercept BCI feeds, scrubbing subconscious feedback before passing the intentional commands to the host device.

If your mind is the product, then guarding its gateway is the defining activist battle of the decade.`,
    publisher: "NeuroTech Daily",
    publishedAt: "1d ago",
    imageUrl: "https://images.unsplash.com/photo-1507668077129-56e32842fceb?q=80&w=600&auto=format&fit=crop",
    likes: 870,
    commentsCount: 142,
    isBreaking: false,
    views: 1800
  }
];

export const mockSurveys: Survey[] = [
  {
    id: "s1",
    title: "Tattle Checkpoint",
    questions: [
      {
        id: "q1",
        question: "Which trend are you most excited about for the next 5 years?",
        options: [
          "Decentralized Off-Grid Internet (Mesh)",
          "Cognitive Liberty & Mind Privacy",
          "Direct-to-Device Cryptographic Voting",
          "Virtual Micronations & Cloud Cooperatives"
        ]
      },
      {
        id: "q2",
        question: "How do you primary consume news outside of Tattle?",
        options: [
          "Short-form Video (TikTok / Reels)",
          "X / Discord Communities",
          "Traditional Outlets (NYT, BBC, etc.)",
          "I actively avoid other news streams"
        ]
      },
      {
        id: "q3",
        question: "Are you willing to participate in brief surveys to keep your content 100% ad-free?",
        options: [
          "Yes, I prefer opinions over ads",
          "Sometimes, if the questions are short",
          "No, show me standard video ads",
          "I'd pay for a direct premium tier"
        ]
      }
    ]
  }
];
